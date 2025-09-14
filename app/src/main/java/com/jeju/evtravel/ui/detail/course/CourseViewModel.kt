package com.jeju.evtravel.ui.detail.course

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.jeju.evtravel.domain.model.Course
import com.jeju.evtravel.domain.usecase.TourPlaceDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val TAG = "CourseVM"

@HiltViewModel
class CourseViewModel @Inject constructor(
    private val tourPlaceDetailUseCase: TourPlaceDetailUseCase,
    private val firestore: FirebaseFirestore
): ViewModel() {

    private val _state = MutableStateFlow(CourseUiState())
    val state: StateFlow<CourseUiState> = _state

    fun fetchCoursesForPlace(placeId: Long?) {
        if (placeId == null) {
            _state.value = _state.value.copy(loading = false, error = "잘못된 장소 ID")
            return
        }

        Log.d(TAG, "Fetching courses for placeId: $placeId")
        _state.value = _state.value.copy(loading = true, error = null)

        viewModelScope.launch {
            try {
                val querySnapshot = firestore.collection("course")
                    .whereEqualTo("id", placeId)
                    .get()
                    .await()

                val course = querySnapshot.toObjects(Course::class.java).firstOrNull()
                if (course != null) {
                    Log.d(TAG, "코스 정보 변환 성공: ${course.course_info.firstOrNull()?.course_name}")
                    fetchImagesForCoursePlaces(course)
                } else {
                    Log.d(TAG, "해당 placeId에 대한 코스를 찾을 수 없음.")
                    _state.value = _state.value.copy(loading = false, data = null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "코스 정보 조회 실패: ${e.message}", e)
                _state.value = _state.value.copy(loading = false, error = e.message)
            }
        }
    }

    private fun fetchImagesForCoursePlaces(course: Course) {
        viewModelScope.launch {
            val courseInfo = course.course_info.firstOrNull() ?: return@launch

            val updatedPlaces = courseInfo.places.map { place ->
                async {
                    // CoursePlace의 id (Kakao Place ID)가 없으면 원본을 그대로 반환
                    if (place.id.isNullOrBlank()) {
                        place
                    } else {
                        try {
                            // 1단계: place.id (Kakao ID)로 Firestore에서 contentId를 조회
                            val document = firestore.collection("kakaoPlaceList").document(place.id).get().await()
                            if (!document.exists()) {
                                Log.w(TAG, "'kakaoPlaceList'에 문서 없음: ${place.id}")
                                return@async place // 문서가 없으면 원본 반환
                            }

                            val contentId = document.getString("contentId")
                            if (contentId.isNullOrBlank()) {
                                Log.w(TAG, "'kakaoPlaceList' 문서에 contentId 필드가 없음: ${place.id}")
                                return@async place // contentId가 없으면 원본 반환
                            }

                            // 2단계: 조회한 contentId로 Tour API 호출
                            val detail = tourPlaceDetailUseCase(contentId)

                            // 3단계: 이미지 URL 추출 및 CoursePlace 객체 업데이트
                            if (detail != null) {
                                val imageUrl = detail.firstImage?.takeIf { it.isNotBlank() } ?: detail.firstImage2
                                Log.d(TAG, "이미지 URL 가져오기 성공: ${place.name} -> $imageUrl")
                                place.copy(imageUrl = imageUrl) // 이미지 URL을 포함하여 새로운 객체 반환
                            } else {
                                Log.w(TAG, "TourAPI에서 contentId에 대한 정보 없음: $contentId")
                                place // TourAPI 결과가 없으면 원본 반환
                            }

                        } catch (e: Exception) {
                            Log.e(TAG, "${place.name}의 이미지 URL 가져오기 전체 과정 실패", e)
                            place // 어떤 단계든 실패하면 원본 반환
                        }
                    }
                }
            }.awaitAll() // 모든 비동기 작업이 끝날 때까지 대기

            // 이미지 URL이 추가된 장소 목록으로 Course 객체를 새로 만듦
            val updatedCourse = course.copy(
                course_info = listOf(courseInfo.copy(places = updatedPlaces))
            )

            // 최종적으로 UI 상태를 업데이트
            _state.value = _state.value.copy(loading = false, data = updatedCourse)
        }
    }
}

data class CourseUiState(
    val loading: Boolean = false,
    val data: Course? = null,
    val error: String? = null
)