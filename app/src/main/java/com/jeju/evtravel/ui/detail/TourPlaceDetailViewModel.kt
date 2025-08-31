package com.jeju.evtravel.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeju.evtravel.domain.model.TourPlaceDetail
import com.jeju.evtravel.domain.usecase.TourPlaceDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TourPlaceDetailUi(
    val id: String,
    val title: String,
    val address: String?,
    val tel: String?,
    val overview: String?,     // HTML일 수 있음 → UI에서 처리
    val imageUrl: String?,
    val contentTypeId: String? // 상세 응답에 없으므로 null
)

data class TourPlaceDetailState(
    val loading: Boolean = true,
    val error: String? = null,
    val data: TourPlaceDetailUi? = null
)

@HiltViewModel
class TourPlaceDetailViewModel @Inject constructor(
    private val detailUseCase: TourPlaceDetailUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // NavHost route = "placeTourDetail/{contentId}"와 키를 반드시 일치시켜야 함
    private val contentId: String =
        checkNotNull(savedStateHandle["contentId"]) { "contentId is required" }

    private val _state = MutableStateFlow(TourPlaceDetailState())
    val state: StateFlow<TourPlaceDetailState> = _state

    init {
        load()
    }

    fun load() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            runCatching {
                detailUseCase(contentId)   // UseCase가 TourPlaceDetail 반환
            }.onSuccess { detail ->
                _state.value = TourPlaceDetailState(
                    loading = false,
                    data = detail.toUi()
                )
            }.onFailure { e ->
                _state.value = TourPlaceDetailState(
                    loading = false,
                    error = e.message ?: "정보를 불러오지 못했습니다."
                )
            }
        }
    }

    private fun TourPlaceDetail.toUi(): TourPlaceDetailUi =
        TourPlaceDetailUi(
            id = contentId ?: "",                    // detail 모델의 contentId
            title = title ?: "",
            address = addr1 ?: addr2,                // addr1 우선, 없으면 addr2
            tel = tel,
            overview = overview,
            imageUrl = firstImage ?: firstImage2,
            contentTypeId = null                     // 상세엔 없음(필요하면 모델/매퍼 확장)
        )
}
