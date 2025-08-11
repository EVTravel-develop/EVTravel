package com.jeju.evtravel.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeju.evtravel.domain.usecase.SearchNearbyPlacesUseCase
import com.kakao.vectormap.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val searchNearbyPlacesUseCase: SearchNearbyPlacesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Idle)
    val uiState: StateFlow<MapUiState> = _uiState

    // 지도 상태 저장 변수
    var lastCenter: LatLng? = null
    var lastZoomLevel: Int? = null
    var lastSelectedPlaceId: String? = null
    var lastUserLocation: LatLng? = null
    // 복원 여부
    var isMapRestored = false

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
            }.onFailure { e ->
                _uiState.value = MapUiState.Error(e.message ?: "unknown error")
            }
        }
    }

    // 지도에서 쓰는 재검색 함수(중심 기준)
    fun searchAroundCenter(radius: Int = 1500) {
        val center = lastCenter ?: return
        searchNearby(
            query = "전기차 충전소", // 혹은 타입에 따라 바꾸려면 파라미터로 받기
            longitude = center.longitude,
            latitude = center.latitude,
            radius = radius
        )
    }
}