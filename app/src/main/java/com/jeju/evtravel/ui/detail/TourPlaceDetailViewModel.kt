package com.jeju.evtravel.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.domain.repository.PlaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaceDetailUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val data: Place? = null
)

data class PlaceDetailUi(
    val place: Place,
    val tourPlace: TourPlaceDetailUi? = null
)

data class TourPlaceDetailUi(
    val id: String?,
    val overview: String?,
    val imageUrl: String?
)

@HiltViewModel
class TourPlaceDetailViewModel @Inject constructor(
    private val placeRepo: PlaceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // NavHost에서 route = "placeDetail/{placeId}" 로 넘긴 값
    private val placeId: String =
        checkNotNull(savedStateHandle.get<String>("placeId")) { "placeId is required" }

    // 출발 화면에서 미리 넣어둔 캐시(선택)
    private val cachedPlace: Place? = savedStateHandle.get<Place>("cachedPlace")

    private val _state = MutableStateFlow(PlaceDetailUiState(loading = true))
    val state: StateFlow<PlaceDetailUiState> = _state

    init {
        // 1) 캐시 즉시 반영
        cachedPlace?.let { _state.value = PlaceDetailUiState(loading = false, data = it) }

        // 2) 저장소에서 보강(캐시 히트 기대)
        viewModelScope.launch {
            runCatching { placeRepo.getPlaceById(placeId) }
                .onSuccess { _state.value = PlaceDetailUiState(data = it) }
                .onFailure { e ->
                    if (e is kotlin.coroutines.cancellation.CancellationException) throw e
                    // 캐시가 이미 보이는 경우는 그대로 두고, 아니면 에러
                    if (_state.value.data == null) {
                        _state.value = PlaceDetailUiState(error = e.message ?: "불러오기 실패")
                    }
                }
        }
    }

    fun reload() {
        viewModelScope.launch {
            runCatching { placeRepo.getPlaceById(placeId) }
                .onSuccess { _state.value = PlaceDetailUiState(data = it) }
                .onFailure { e ->
                    if (e is kotlin.coroutines.cancellation.CancellationException) throw e
                    _state.value = _state.value.copy(error = e.message ?: "불러오기 실패")
                }
        }
    }
}
