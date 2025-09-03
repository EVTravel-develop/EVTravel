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
import com.kakao.vectormap.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun clearSelection() { _selectedPlace.value = null }
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

    /** 상세 충전소 검색 */
    fun selectPlace(place: Place, forceRefresh: Boolean = false, onComplete: () -> Unit = {}) {
        selectJob?.cancel()
        selectJob = viewModelScope.launch {
            try {
                _selectedPlaceError.value = null
                Log.d("MVM_selectPlace", "장소 선택: ${place.name} (${place.latitude}, ${place.longitude}) id=${place.id}")

                val curr0 = _selectedPlace.value
                if (curr0 == null || curr0.id != place.id) {
                    _selectedPlace.value = place
                }
                _isSelectedPlaceLoading.value = true

                // 1) 좌표 → 지역 코드
                val x = place.longitude
                val y = place.latitude
                val regionCode = regionCodeRepository.getRegionCodeFromCoord(x, y)
                    ?: run {
                        _selectedPlaceError.value = "지역 코드를 찾지 못했습니다."
                        return@launch
                    }
                val zcode = regionCode.zcode
                val zscode = regionCode.zscode
                Log.d("MVM_selectPlace", "주소 변환 → zcode=$zcode, zscode=$zscode")

                // 2) Firestore에서 카카오 id로 statId 배열 조회
                val kakaoId = place.id
                val statIds = fetchStatIdsForKakaoId(kakaoId)

                if (statIds.isEmpty()) {
                    // 정책: statId가 없으면 비우거나(엄격) 또는 지역 전체를 불러와 보여주기(관대한 fallback)
//                    _selectedPlaceError.value = "연결된 충전소(statId)가 없습니다."
                    Log.w("MVM_selectPlace", "kakao_id=$kakaoId → statIds 비어있음")
                    // 필요시 fallback:
                    val fallback = chargerListRepository.fetchChargers(zcode, zscode, statId = "")
                    _selectedPlace.value = place.copy(chargerList = fallback)
                    _isSelectedPlaceLoading.value = false
                    onComplete()
                    return@launch
                }

                // 3) statId별로 공공데이터 API 호출 (캐시/강제새로고침 반영)
                val merged = mutableListOf<ChargerInfo>()
                for (sid in statIds) {
                    val key = Triple(zcode, zscode, sid)

                    val listForSid: List<ChargerInfo> = if (forceRefresh) {
                        chargerByStatCache.remove(key)
                        runCatching {
                            chargerListRepository.fetchChargers(
                                zcode = zcode,
                                zscode = zscode,
                                statId = sid
                            )
                        }.onFailure { e ->
                            Log.e("MVM_selectPlace", "충전소 API 실패 sid=$sid", e)
                        }.getOrElse { emptyList() }
                            .also { chargerByStatCache[key] = it }
                    } else {
                        chargerByStatCache.getOrPut(key) {
                            runCatching {
                                chargerListRepository.fetchChargers(
                                    zcode = zcode,
                                    zscode = zscode,
                                    statId = sid
                                )
                            }.onFailure { e ->
                                Log.e("MVM_selectPlace", "충전소 API 실패 sid=$sid", e)
                            }.getOrElse { emptyList() }
                        }
                    }

                    Log.d("MVM_selectPlace", "statId=$sid → ${listForSid.size}개 수신")
                    merged += listForSid
                }

                Log.d("MVM_selectPlace", "총 합계(중복 포함) ${merged.size}개")

                // (선택) 중복 제거: 같은 statId/chgerId 조합 중복이 있을 수 있으니 고유화
                val dedup = merged
                    .distinctBy { c ->
                        // 도메인 모델 정의에 맞게 키 선택
                        "${runCatching { c.statId }.getOrNull()}#${runCatching { c.chargerId }.getOrNull()}"
                    }

                Log.d("MVM_selectPlace", "중복 제거 후 ${dedup.size}개")

                // 4) Place 업데이트 (선택 변경이 있으면 폐기)
                val updated = try { place.copy(chargerList = dedup) } catch (_: Throwable) {
                    place.chargerList = dedup; place
                }
                val curr1 = _selectedPlace.value
                if (curr1 != null && curr1.id != place.id) {
                    Log.d("MVM_selectPlace", "선택 변경 감지: ${curr1.id} != ${place.id}, 업데이트 폐기")
                    return@launch
                }
                if (curr1 != updated) {
                    _selectedPlace.value = updated
                    placeIndex[updated.id] = updated
                }

            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                _selectedPlaceError.value = "알 수 없는 오류가 발생했습니다."
                Log.e("MVM_selectPlace", "selectPlace error", e)
            } finally {
                _isSelectedPlaceLoading.value = false
                runCatching { onComplete() }
            }
        }
    }

    /** 상새 중전소 검색 다시시도 */
    fun fetchCharger(placeId: String, forceRefresh: Boolean = false) {
        if (forceRefresh) placeIndex.remove(placeId)

        // 마지막 검색/선택 캐시에서 place 찾아서 재요청
        val place = placeIndex[placeId] ?: _selectedPlace.value?.takeIf { it.id == placeId }
        if (place != null) {
            selectPlace(place, forceRefresh = forceRefresh)
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

    fun focusAndSelect(place: Place, radius: Int = 2000, defaultZoom: Int = 15) {
        // 카메라/선택 복원 상태 세팅
        lastCenter = LatLng.from(place.latitude, place.longitude)
        lastZoomLevel = defaultZoom
        lastSelectedPlaceId = place.id
        isMapRestored = false

        // 상세(충전기) 로딩
        selectPlace(place)

        // 주변 재검색
        searchNearby(
            query = "제주 전기차 충전소",
            longitude = place.longitude,
            latitude = place.latitude,
            radius = radius
        )

        // 다음 진입 시 현재위치 자동 세팅을 1회 건너뛰기
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