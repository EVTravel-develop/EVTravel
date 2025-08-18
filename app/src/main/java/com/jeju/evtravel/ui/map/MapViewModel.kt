package com.jeju.evtravel.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeju.evtravel.data.repository.ChargerRepository
import com.jeju.evtravel.data.repository.RegionCodeRepository
import com.jeju.evtravel.domain.model.ChargerInfo
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.domain.usecase.SearchNearbyPlacesUseCase
import com.kakao.vectormap.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class MapViewModel @Inject constructor(
    private val searchNearbyPlacesUseCase: SearchNearbyPlacesUseCase,
    private val regionCodeRepository: RegionCodeRepository,
    private val chargerRepository: ChargerRepository
) : ViewModel() {

    // 도로명 정규화 표현식
    companion object {
        private const val TAG = "MapVM"
        private const val MIN_RADIUS = 0
        private const val MAX_RADIUS = 20_000
        private val CLEAN_RE = Regex("""[\s\-\(\)\[\]]+""")
        // val regex = Regex("""[가-힣A-Za-z0-9]+(동|로|길|번길)\s?\d+[가-힣A-Za-z0-9\s]*""")
        // val regex = Regex("""\b[가-힣A-Za-z0-9]+(동|로|길|번길)\s?\d+[가-힣A-Za-z0-9\s]*\b""")
        private val ROAD_KEY_RE = Regex("""\b[가-힣A-Za-z0-9]+(동|로|길|번길)\s?\d+[가-힣A-Za-z0-9\s]*\b""")
    }

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

    fun clearSelection() { _selectedPlace.value = null }

    /** 최근 검색 결과/선택 결과를 빠르게 찾기 위한 캐시 */
    private val chargerCache = mutableMapOf<Pair<String, String>, List<ChargerInfo>>()
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
                Log.e("M_V_M", "searchNearby error", e)
            }
        }
    }

    /** 상새 중전소 검색 */
    fun selectPlace(place: Place, onComplete: () -> Unit = {}) {
        selectJob?.cancel()
        selectJob = viewModelScope.launch {
            try {
                _selectedPlaceError.value = null
                Log.d(
                    "MVM_selectPlace",
                    "장소 이름 + 좌표 호출: ${place.name} (${place.latitude}, ${place.longitude})"
                )
                Log.d("MVM_selectPlace", "장소 주소 호출: ${place.roadAddress} | ${place.address}")

                val curr0 = _selectedPlace.value
                if (curr0 == null || curr0.id != place.id) {
                    _selectedPlace.value = place
                }

                _isSelectedPlaceLoading.value = true // 로딩 시작 상태

                // 지역 코드 추출
                val x = place.longitude
                val y = place.latitude

                val regionCode = regionCodeRepository.getRegionCodeFromCoord(x, y)
                    ?: run {
                        _selectedPlaceError.value = "지역 코드를 찾지 못했습니다."
                        return@launch
                    }

                Log.d(
                    "MVM_selectPlace",
                    "주소 변환 → zcode=${regionCode.zcode}, zscode=${regionCode.zscode}"
                )

                val cacheKey = regionCode.zcode to regionCode.zscode
                val chargers = chargerCache.getOrPut(cacheKey) {
                    runCatching {
                        chargerRepository.fetchChargers(
                            zcode = regionCode.zcode,
                            zscode = regionCode.zscode
                        )
                    }.onFailure { e ->
                        _selectedPlaceError.value = "충전소 정보를 불러오지 못했습니다."
                        Log.e("MVM_selectPlace", "충전소 API 호출 실패", e)
                    }.getOrElse { emptyList() }
                }

                Log.d(
                    "MVM_selectPlace",
                    "공공데이터 API 호출 완료 (cached=${cacheKey in chargerCache}): 총 ${chargers.size}개"
                )

                // 도로명 정규화
                val normalizedRoad = normalizeRoadAddress(place.roadAddress.orEmpty())
                val kakaoKey = extractRoadKey(normalizedRoad)
                // 같은 장소(도로명 주소 + lat/lng 일치)인 충전기만 필터링
                val matched = chargers.filter { extractRoadKey(it.address).let { k -> k.isNotEmpty() && k == kakaoKey } }

                Log.d("MVM_selectPlace", "매칭된 충전기: ${matched.size}개")

                val updated = try {
                    place.copy(chargerList = matched)
                } catch (_: Throwable) {
                    place.chargerList = matched
                    place
                }

                // 사용자가 다른 마커로 바꿨다면 폐기
                val curr1 = _selectedPlace.value
                if (curr1 != null && curr1.id != place.id) {
                    Log.d("MVM_selectPlace", "선택 변경 감지: ${curr1.id} != ${place.id}, 업데이트 폐기")
                    return@launch
                }

                // 동일 값이면 불필요한 리컴포지션 방지
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
                _isSelectedPlaceLoading.value = false   // 로딩 완료 -> 종료
                runCatching { onComplete() }
            }
        }
    }

    /** 상새 중전소 검색 다시시도 */
    fun fetchCharger(placeId: String) {
        // 마지막 검색/선택 캐시에서 place 찾아서 재요청
        val place = placeIndex[placeId] ?: _selectedPlace.value?.takeIf { it.id == placeId }
        if (place != null) {
            selectPlace(place)
        } else {
            Log.w("MapVM", "fetchCharger: place not found for id=$placeId")
        }
    }

/** 지도에서 쓰는 재검색 (중심 기준) */
    fun searchAroundCenter(radius: Int = 2000) {
        val center = lastCenter ?: return
        searchNearby(
            query = "전기차 충전소",
            longitude = center.longitude,
            latitude = center.latitude,
            radius = radius
        )
    }

    private fun normalizeRoadAddress(address: String): String {
        if (address.isBlank()) return address
        val trimmed = address.trim()
        val replacements = mapOf(
            "서울 " to "서울특별시 ",
            "서울시 " to "서울특별시 ",
            "부산 " to "부산광역시 ",
            "부산시 " to "부산광역시 ",
            "대구 " to "대구광역시 ",
            "대구시 " to "대구광역시 ",
            "인천 " to "인천광역시 ",
            "인천시 " to "인천광역시 ",
            "광주 " to "광주광역시 ",
            "광주시 " to "광주광역시 ",
            "대전 " to "대전광역시 ",
            "대전시 " to "대전광역시 ",
            "울산 " to "울산광역시 ",
            "울산시 " to "울산광역시 ",
            "세종 " to "세종특별자치시 ",
            "제주 " to "제주특별자치도 ",
            "제주시 " to "제주특별자치도 "
        )
        for ((k, v) in replacements) {
            if (trimmed.startsWith(k)) {
                return trimmed.replaceFirst(k, v)
            }
        }
        return trimmed
    }

    fun extractRoadKey(address: String): String {
        if (address.isBlank()) return ""
        val raw = ROAD_KEY_RE.find(address)?.value ?: return ""
        val compact = raw.replace(CLEAN_RE, "")
        return compact.lowercase()
    }

    var skipAutoCenterOnce: Boolean = false

    fun focusAndSelect(place: Place, radius: Int = 2000, defaultZoom: Int = 15) {
        // 1) 카메라/선택 복원 상태 세팅
        lastCenter = LatLng.from(place.latitude, place.longitude)
        lastZoomLevel = defaultZoom
        lastSelectedPlaceId = place.id
        isMapRestored = false

        // 2) 상세(충전기) 로딩
        selectPlace(place)

        // 3) 주변 재검색
        searchNearby(
            query = "전기차 충전소",
            longitude = place.longitude,
            latitude = place.latitude,
            radius = radius
        )

        // 4) 다음 진입 시 현재위치 자동 세팅을 1회 건너뛰기
        skipAutoCenterOnce = true
    }
}