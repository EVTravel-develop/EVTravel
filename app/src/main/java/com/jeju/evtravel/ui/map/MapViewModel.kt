package com.jeju.evtravel.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import com.jeju.evtravel.data.repository.RegionCodeRepository
import com.jeju.evtravel.domain.model.ChargerInfo
import com.jeju.evtravel.domain.model.CoursePlace
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.domain.repository.PlaceRepository
import com.jeju.evtravel.domain.usecase.SearchNearbyPlacesUseCase
import com.jeju.evtravel.domain.usecase.TourPlaceDetailUseCase
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

private const val TAG = "MapVM"
@HiltViewModel
class MapViewModel @Inject constructor(
    private val searchNearbyPlacesUseCase: SearchNearbyPlacesUseCase,
    private val placeRepository: PlaceRepository,
    private val regionCodeRepository: RegionCodeRepository,
    private val chargerListRepository: ChargerListRepository,
    private val tourPlaceDetailUseCase: TourPlaceDetailUseCase,
    private val firestore: FirebaseFirestore
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
                Log.d(TAG, "searchNearby 근처 검색 성공: ${places.size} places")
            }.onFailure { e ->
                _uiState.value = MapUiState.Error(e.message ?: "unknown error")
                Log.e(TAG, "searchNearby error: ${e}", e)
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
//                _selectedPlaceError.value = "알 수 없는 오류가 발생했습니다."
            } finally {
                _isSelectedPlaceLoading.value = false
                runCatching { onComplete() }
            }
        }
    }

    fun selectPlace(
        place: Place,
        kind: SummaryKind,
        forceRefresh: Boolean = false
    ) {
        _selectedPlace.value = place
        _selectedKind.value = kind
        _isSelectedPlaceLoading.value = true
        _selectedPlaceError.value = null

        // ✅ 2. 추가 정보(Tour API) 조회를 시작합니다. (충전소 여부와 관계없이 항상 시도)
        fetchTourDetailsAndUpdateState(place)

        // 3. 만약 충전소라면, 충전소 상세 정보 조회를 시작합니다.
        if (kind == SummaryKind.CHARGER) {
            selectPlaceCharger(place, forceRefresh)
        } else {
            // 충전소가 아닌 일반 장소라면 로딩 상태만 종료
            _isSelectedPlaceLoading.value = false
        }
    }

    fun cachePlace(place: Place) {
        placeRepository.cachePlace(place)
    }

    suspend fun findPlaceFromCoursePlace(coursePlace: CoursePlace): Place? {
        // 검색에 필요한 최소 정보가 없으면 null 반환
        if (coursePlace.x == null || coursePlace.y == null || coursePlace.name.isNullOrBlank()) {
            Log.w(TAG, "CoursePlace에 검색 정보(이름, 좌표)가 부족합니다.")
            return null
        }

        return try {
            // 장소 이름과 좌표로 주변을 다시 검색합니다.
            // radius를 50m로 매우 좁게 설정하여 정확도를 높입니다.
            val searchResults = searchNearbyPlacesUseCase(
                query = coursePlace.name,
                x = coursePlace.x,
                y = coursePlace.y,
                radius = 50
            )

            // 검색 결과 중에서 CoursePlace의 id와 일치하는 것을 우선적으로 찾습니다.
            val matchedPlace = searchResults.find { it.id == coursePlace.id }
            if (matchedPlace != null) {
                Log.d(TAG, "ID로 정확한 장소를 찾았습니다: ${matchedPlace.name}")
                return matchedPlace
            }

            // ID가 일치하는 것이 없으면, 가장 첫 번째 결과를 반환합니다.
            Log.d(TAG, "ID가 일치하는 장소가 없어 첫 번째 검색 결과를 사용합니다.")
            searchResults.firstOrNull()

        } catch (e: Exception) {
            Log.e(TAG, "${coursePlace.name} 재검색 중 API 오류 발생", e)
            null // 오류 발생 시 null 반환
        }
    }

    private fun fetchTourDetailsAndUpdateState(place: Place) {
        viewModelScope.launch {
            try {
                val document = firestore.collection("kakaoPlaceList").document(place.id).get().await()
                if (document.exists()) {
                    val contentId = document.getString("contentId")
                    if (!contentId.isNullOrBlank()) {
                        Log.d(TAG, "Firestore에서 contentId '${contentId}' 찾음. Tour API 호출.")
                        val tourDetail = tourPlaceDetailUseCase(contentId)
                        val imageUrl = tourDetail.firstImage?.takeIf { it.isNotBlank() } ?: tourDetail.firstImage2

                        // 중요: 현재 StateFlow의 값을 가져와서 복사해야 다른 정보(충전소 목록)가 덮어씌워지지 않음
                        val currentPlace = _selectedPlace.value
                        if (currentPlace != null && currentPlace.id == place.id) {
                            val updatedPlace = currentPlace.copy(
                                overview = tourDetail.overview,
                                imageUrl = imageUrl
                            )
                            _selectedPlace.value = updatedPlace
                            Log.d(TAG, "Tour 정보로 selectedPlace 업데이트 완료. imageUrl: $imageUrl")
                        }
                    } else {
                        Log.w(TAG, "Firestore에 contentId가 없어 Tour API를 호출하지 않음.")
                    }
                } else {
                    Log.w(TAG, "Firestore에 해당 placeId 문서 없음: ${place.id}")
                }
            } catch (e: Exception) {
                // Tour 정보는 부가 정보이므로 실패해도 에러를 표시하지 않고 로그만 남김
                Log.e(TAG, "Tour 정보 조회 실패 (placeId: ${place.id})", e)
            }
        }
    }

    /** 상새 중전소 검색 다시시도 */
    fun fetchCharger(placeId: String, forceRefresh: Boolean = false) {
        val place = placeIndex[placeId] ?: _selectedPlace.value?.takeIf { it.id == placeId }
        if (place != null) {
            selectPlace(place, SummaryKind.CHARGER, forceRefresh = forceRefresh)
        } else {
            Log.w(TAG, "fetchCharger: place not found for id=$placeId")
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
        selectPlace(place, kind)

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