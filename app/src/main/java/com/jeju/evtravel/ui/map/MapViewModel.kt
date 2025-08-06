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
    // 복원 여부
    var isMapRestored = false

    fun searchNearby(query: String, longitude: Double, latitude: Double, radius: Int = 1000) {
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
}