package com.jeju.evtravel.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import com.jeju.evtravel.data.repository.RegionCodeRepository
import com.jeju.evtravel.domain.model.ChargerInfo
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.domain.usecase.SearchNearbyPlacesUseCase
import com.jeju.evtravel.ui.detail.SummaryKind
import com.kakao.vectormap.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import com.jeju.evtravel.data.repository.ChargerRepository as ChargerListRepository

@HiltViewModel
class MapViewModel @Inject constructor(
    private val searchNearbyPlacesUseCase: SearchNearbyPlacesUseCase,
    private val regionCodeRepository: RegionCodeRepository,
    private val chargerListRepository: ChargerListRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Idle)
    val uiState: StateFlow<MapUiState> = _uiState

    private val _selectedPlace = MutableStateFlow<Place?>(null)
    val selectedPlace: StateFlow<Place?> = _selectedPlace

    // 장소 타입 보관
    private val _selectedKind = MutableStateFlow<SummaryKind?>(null)
    val selectedKind: StateFlow<SummaryKind?> = _selectedKind

    // 상세 패널용 로딩/에러 상태 보관
    private val _isSelectedPlaceLoading = MutableStateFlow(false)
    val isSelectedPlaceLoading: StateFlow<Boolean> = _isSelectedPlaceLoading

    private val _selectedPlaceError = MutableStateFlow<String?>(null)
    val selectedPlaceError: StateFlow<String?> = _selectedPlaceError

    // 지도 상태 저장 변수
    var lastCenter: LatLng? = null
    var lastZoomLevel: Int? = null
    var lastSelectedPlaceId: String? = null
    var lastUserLocation: LatLng? = null
    // 복원 여부
    var isMapRestored = false

    var lastSearchCenter: LatLng? = null
    var lastSearchZoomLevel: Int? = null

    fun clearSelection() {
        _selectedPlace.value = null
        _selectedKind.value = null
    }
    private val chargerByStatCache = mutableMapOf<Triple<String, String, String>, List<ChargerInfo>>()
    private var placeIndex = mutableMapOf<String, Place>()  // id -> Place
    private var currentPlaces: List<Place> = emptyList()    // 리스트 보관 (디버그/순회용

    // selectPlace 동시 호출 안정화
    private var selectJob: Job? = null

    /** 주변 검색 */
    fun searchNearby(query: String, longitude: Double, latitude: Double, radius: Int = 2000) {
        viewModelScope.launch {
            _uiState.value = MapUiState.Loading
            runCatching {
                searchNearbyPlacesUseCase(
                    query = query,
                    x = longitude,
                    y = latitude,
                    radius = radius
                )
            }.onSuccess { places ->
                currentPlaces = places
                placeIndex.clear()
                places.forEach { placeIndex[it.id] = it }

                _uiState.value = MapUiState.Success(places)
                Log.d("M_V_M", "근처 검색 성공: ${places.size} places")
            }.onFailure { e ->
                _uiState.value = MapUiState.Error(e.message ?: "unknown error")
                Log.e("M_V_M", "searchNearby error: ${e}", e)
            }
        }
    }

    fun kindFor(place: Place, preferred: SummaryKind? = null): SummaryKind {
        if (preferred != null) return preferred
        return if (isEvCharger(place)) SummaryKind.CHARGER else SummaryKind.PLACE
    }

    private fun isEvCharger(place: Place): Boolean {
        val cat = (place.category ?: "").lowercase()
        if (cat.contains("전기차 충전소")) return true
        return false
    }

    private fun selectPlaceCharger(
        place: Place,
        forceRefresh: Boolean = false,
        onComplete: () -> Unit = {}
    ) {
        selectJob?.cancel()
        selectJob = viewModelScope.launch {
            try {
                _selectedPlaceError.value = null
                val curr0 = _selectedPlace.value
                if (curr0 == null || curr0.id != place.id) _selectedPlace.value = place
                _isSelectedPlaceLoading.value = true

                // --- 아래는 기존 CHARGER 분기 안에 있던 코드 그대로 이동 ---
                val x = place.longitude
                val y = place.latitude
                val regionCode = regionCodeRepository.getRegionCodeFromCoord(x, y)
                    ?: run {
                        _selectedPlaceError.value = "지역 코드를 찾지 못했습니다."
                        return@launch
                    }
                val zcode = regionCode.zcode
                val zscode = regionCode.zscode

                val kakaoId = place.id
                val statIds = fetchStatIdsForKakaoId(kakaoId)

                if (statIds.isEmpty()) {
                    val updated = place.copy(chargerList = emptyList())
                    _selectedPlace.value = updated
                    placeIndex[updated.id] = updated
                    // _selectedPlaceError.value = "이 장소는 매핑된 충전소가 없습니다." // 안내를 띄우고 싶으면 주석 해제
                    return@launch
                }

                val merged = coroutineScope {
                    statIds.map { sid ->
                        async {
                            val key = Triple(zcode, zscode, sid)
                            val listForSid: List<ChargerInfo> =
                                if (forceRefresh) {
                                    chargerByStatCache.remove(key)
                                    runCatching { chargerListRepository.fetchChargers(zcode, zscode, sid) }
                                        .getOrElse { emptyList() }
                                        .also { chargerByStatCache[key] = it }
                                } else {
                                    chargerByStatCache.getOrPut(key) {
                                        runCatching { chargerListRepository.fetchChargers(zcode, zscode, sid) }
                                            .getOrElse { emptyList() }
                                    }
                                }
                            listForSid
                        }
                    }.awaitAll().flatten()
                }

                val dedup = merged.distinctBy { c ->
                    "${runCatching { c.statId }.getOrNull()}#${runCatching { c.chargerId }.getOrNull()}"
                }

                val updated = place.copy(chargerList = dedup)
                val curr1 = _selectedPlace.value
                if (curr1 != null && curr1.id != place.id) return@launch
                if (curr1 != updated) {
                    _selectedPlace.value = updated
                    placeIndex[updated.id] = updated
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                _selectedPlaceError.value = "알 수 없는 오류가 발생했습니다."
            } finally {
                _isSelectedPlaceLoading.value = false
                runCatching { onComplete() }
            }
        }
    }

    fun selectPlace(
        place: Place,
        kind: SummaryKind,
        forceRefresh: Boolean = false,
        onComplete: () -> Unit = {}
    ) {
        _selectedKind.value = kind

        if (kind == SummaryKind.CHARGER) {
            selectPlaceCharger(place, forceRefresh, onComplete)
            return
        } else {
            selectJob?.cancel()
            selectJob = viewModelScope.launch {
                _selectedPlaceError.value = null
                _isSelectedPlaceLoading.value = false
                _selectedPlace.value = place
                onComplete()
            }
        }
    }

    /** 상새 중전소 검색 다시시도 */
    fun fetchCharger(placeId: String, forceRefresh: Boolean = false) {
        if (forceRefresh) placeIndex.remove(placeId)

        // 마지막 검색/선택 캐시에서 place 찾아서 재요청
        val place = placeIndex[placeId] ?: _selectedPlace.value?.takeIf { it.id == placeId }
        if (place != null) {
            selectPlace(place, SummaryKind.CHARGER, forceRefresh = forceRefresh)
        } else {
            Log.w("MapVM", "fetchCharger: place not found for id=$placeId")
        }
    }

    /** 지도에서 쓰는 재검색 (중심 기준) */
    fun markSearched(center: LatLng, zoom: Int?) {
        lastSearchCenter = center
        lastSearchZoomLevel = zoom
    }

    var skipAutoCenterOnce: Boolean = false

    fun focusAndSelect(
        place: Place,
        radius: Int = 2000,
        defaultZoom: Int = 15
    ) {
        val kind = kindFor(place)
        focusAndSelect(place, kind, radius, defaultZoom)
    }

    fun focusAndSelect(
        place: Place,
        kind: SummaryKind,
        radius: Int = 2000,
        defaultZoom: Int = 15
    ) {
        lastCenter = LatLng.from(place.latitude, place.longitude)
        lastZoomLevel = defaultZoom
        lastSelectedPlaceId = place.id
        isMapRestored = false

        _selectedKind.value = kind

        if (kind == SummaryKind.CHARGER) {
            // 충전소는 상세 로딩 + 주변 충전소 재검색 유지
            selectPlace(place, kind)
            searchNearby(
                query = "제주 전기차 충전소",
                longitude = place.longitude,
                latitude = place.latitude,
                radius = radius
            )
        } else {
            // 장소는 선택만 (주변 충전소 재검색은 불필요)
            _selectedPlace.value = place
            _isSelectedPlaceLoading.value = false
        }

        skipAutoCenterOnce = true
    }

    private suspend fun fetchStatIdsForKakaoId(kakaoId: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val snap = Firebase.firestore.collection("kakao_statids").document(kakaoId).get().await()
            if (!snap.exists()) {
                Log.w("MVM_firestore", "문서 없음: kakao_statids/$kakaoId")
                return@withContext emptyList()
            }
            val arr = snap.get("statid") as? List<*>
            arr?.mapNotNull { it?.toString()?.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        } catch (e: FirebaseFirestoreException) {
            Log.e("MVM_firestore", "Firestore 읽기 실패(${e.code}): kakao_id=$kakaoId", e)
            emptyList()
        } catch (e: Exception) {
            Log.e("MVM_firestore", "알 수 없는 Firestore 오류: kakao_id=$kakaoId", e)
            emptyList()
        }
    }
}