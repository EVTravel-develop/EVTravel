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

    // 지도 상태 저장 변수
    var lastCenter: LatLng? = null
    var lastZoomLevel: Int? = null
    var lastSelectedPlaceId: String? = null
    var lastUserLocation: LatLng? = null
    // 복원 여부
    var isMapRestored = false

    fun clearSelection() { _selectedPlace.value = null }

    // 캐시
    private val chargerCache = mutableMapOf<Pair<String, String>, List<ChargerInfo>>()

    // selectPlace 동시 호출 안정화
    private var selectJob: Job? = null

    /** 주변 검색 */
    fun searchNearby(query: String, longitude: Double, latitude: Double, radius: Int) {
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
                _uiState.value = MapUiState.Success(places)
                Log.d("M_V_M", "근처 검색 성공: ${places.size} places")
            }.onFailure { e ->
                _uiState.value = MapUiState.Error(e.message ?: "unknown error")
                Log.e("M_V_M", "searchNearby error", e)
            }
        }
    }

    fun selectPlace(place: Place, onComplete: () -> Unit = {}) {
        selectJob?.cancel()
        selectJob = viewModelScope.launch {
            try {
                Log.d(
                    "MVM_selectPlace",
                    "장소 이름 + 좌표 호출: ${place.name} (${place.latitude}, ${place.longitude})"
                )
                Log.d("MVM_selectPlace", "장소 주소 호출: ${place.roadAddress} | ${place.address}")

                val curr0 = _selectedPlace.value
                if (curr0 == null || curr0.id != place.id) {
                    _selectedPlace.value = place
                }

                val x = place.longitude
                val y = place.latitude

                val regionCode = regionCodeRepository.getRegionCodeFromCoord(x, y)
                if (regionCode == null) {
                    Log.w("MVM_selectPlace", "Error - 지역 코드 추출 실패 x=$x, y=$y")
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
                val matched = chargers.filter { charger ->
                    val key = extractRoadKey(charger.address)
                    key.isNotEmpty() && key == kakaoKey
                }

                Log.d("MVM_selectPlace", "매칭된 충전기: ${matched.size}개")

                val updated = try {
                    place.copy(chargerList = matched)
                } catch (_: Throwable) {
                    place.chargerList = matched
                    place
                }

                // 사용자가 다른 마커로 바꿨는지 확인
                val curr1 = _selectedPlace.value
                if (curr1 != null && curr1.id != place.id) {
                    // 사용자가 다른 장소로 바꿨을 때만 폐기
                    Log.d("MVM_selectPlace", "선택 변경 감지: ${curr1.id} != ${place.id}, 업데이트 폐기")
                    return@launch
                }

                // 동일 값이면 불필요한 리컴포지션 방지
                if (curr1 != updated) {
                    _selectedPlace.value = updated
                }

            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                Log.e("MVM_selectPlace", "selectPlace error", e)
            } finally {
                runCatching { onComplete() }
            }
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
}