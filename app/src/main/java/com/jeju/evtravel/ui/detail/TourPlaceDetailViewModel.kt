package com.jeju.evtravel.ui.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.domain.repository.PlaceRepository
import com.jeju.evtravel.domain.usecase.TourPlaceDetailUseCase
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

private const val TAG = "TourVM"

@HiltViewModel
class TourPlaceDetailViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    private val tourPlaceDetailUseCase: TourPlaceDetailUseCase,
    private val firestore: FirebaseFirestore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // NavHost에서 route = "placeDetail/{placeId}" 로 넘긴 값
    private val placeId: String = checkNotNull(savedStateHandle["placeId"])

    // 출발 화면에서 미리 넣어둔 캐시(선택)
    private val cachedPlace: Place? = savedStateHandle.get<Place>("cachedPlace")

    private val _state = MutableStateFlow(PlaceDetailUiState(loading = true))
    val state: StateFlow<PlaceDetailUiState> = _state

    init {
        loadPlaceDetails()
    }

    private fun loadPlaceDetails() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)

            // 1. cachedPlace가 있으면, 그것을 즉시 초기 데이터로 사용합니다.
            if (cachedPlace != null) {
                Log.d(TAG, "cachedPlace를 사용합니다: ${cachedPlace.name}")
                _state.value = PlaceDetailUiState(loading = false, data = cachedPlace)
                // Tour API 정보(이미지, 설명)를 가져오는 로직은 그대로 실행합니다.
                fetchTourDetails(cachedPlace)
            }
            // 2. cachedPlace가 없으면(예: 앱이 시스템에 의해 종료 후 재생성), Repository에서 찾기를 시도합니다.
            else {
                Log.d(TAG, "cachedPlace가 없어 Repository에서 placeId($placeId)로 조회합니다.")
                try {
                    val placeFromRepo = placeRepository.getPlaceById(placeId)
                    _state.value = PlaceDetailUiState(loading = false, data = placeFromRepo)
                    fetchTourDetails(placeFromRepo)
                } catch (e: Exception) {
                    Log.e(TAG, "Repository 조회 실패", e)
                    _state.value = PlaceDetailUiState(error = e.message ?: "장소 정보를 불러오는데 실패했습니다.")
                }
            }
        }
    }

    private fun fetchTourDetails(place: Place) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Firestore에서 contentId 조회 시작. placeId: ${place.id}")
                firestore.collection("kakaoPlaceList").document(place.id).get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            Log.d(TAG, "Firestore 문서 발견. Data: ${document.data}")
                            val contentId = document.getString("contentId")
                            if (!contentId.isNullOrBlank()) {
                                // contentId가 있으면 UseCase 호출
                                Log.d(TAG, "contentId 추출 성공: '$contentId'")
                                callTourApi(place, contentId)
                            } else {
                                Log.w(TAG, "Firestore에 contentId가 없습니다. (placeId: ${place.id})")
                            }
                        } else {
                            Log.w(TAG, "Firestore에서 문서를 찾을 수 없습니다. (placeId: ${place.id})")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Firestore 조회 실패", e)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Tour 정보 로딩 중 오류 발생", e)
            }
        }
    }

    private fun callTourApi(originalPlace: Place, contentId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "TourAPI 요청 시작. contentId: '$contentId'")
                val tourDetail = tourPlaceDetailUseCase(contentId)
                Log.d(TAG, "TourAPI 응답 성공. overview: ${tourDetail.overview?.take(30)}...")
                var imageUrl = tourDetail.firstImage?.takeIf { it.isNotBlank() } ?: tourDetail.firstImage2

                Log.d(TAG, "원본 이미지 URL: $imageUrl")
                if (imageUrl?.startsWith("http://") == true) {
                    imageUrl = imageUrl.replace("http://", "https://")
                    Log.d(TAG, "HTTPS로 변환된 URL: $imageUrl")
                }

                Log.d(TAG, "최종 이미지 URL: $imageUrl")

                // 기존 Place 정보에 Tour API 정보를 합쳐 새로운 Place 객체 생성
                val updatedPlace = originalPlace.copy(
                    overview = tourDetail.overview,
                    imageUrl = imageUrl
                )
                Log.d(TAG, "UI 상태 업데이트 완료.")

                // 최종적으로 UI 상태 업데이트
                _state.value = _state.value.copy(data = updatedPlace)

            } catch (e: Exception) {
                Log.e(TAG, "Tour API 호출 실패 (contentId: $contentId)", e)
            }
        }
    }

    fun reload() {
        loadPlaceDetails()
    }
}
