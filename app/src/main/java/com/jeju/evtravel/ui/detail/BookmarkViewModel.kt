package com.jeju.evtravel.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeju.evtravel.domain.model.Course
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.service.auth.CourseBookmarkService
import com.jeju.evtravel.service.auth.FirebaseAuthService
import com.jeju.evtravel.service.auth.PlaceBookmarkService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val placeBookmarkService: PlaceBookmarkService,
    private val courseBookmarkService: CourseBookmarkService,
    private val firebaseAuthService: FirebaseAuthService
) : ViewModel() {

    // 장소 북마크 상태
    private val _isPlaceBookmarked = MutableStateFlow(false)
    val isPlaceBookmarked: StateFlow<Boolean> = _isPlaceBookmarked

    // 코스 북마크 상태
    private val _isCourseBookmarked = MutableStateFlow(false)
    val isCourseBookmarked: StateFlow<Boolean> = _isCourseBookmarked

    // 사용자 ID
    private val uid: String? = firebaseAuthService.getUserId()

    fun checkPlaceBookmark(kakaoId: String) {
        viewModelScope.launch {
            val bookmark = placeBookmarkService.getBookmark(uid, kakaoId)
            _isPlaceBookmarked.value = (bookmark != null)
        }
    }

    fun togglePlaceBookmark(place: Place) {
        viewModelScope.launch {
            if (_isPlaceBookmarked.value) {
                placeBookmarkService.deleteBookmark(uid, place.id)
            } else {
                placeBookmarkService.addBookmark(
                    uid = uid,
                    kakaoId = place.id,
                    x = place.longitude,
                    y = place.latitude,
                    placeName = place.name,
                    description = place.overview,
                    imageUrl = place.imageUrl
                )
            }
            _isPlaceBookmarked.value = !_isPlaceBookmarked.value
        }
    }

    fun checkCourseBookmark(courseId: String) {
        viewModelScope.launch {
            val bookmark = courseBookmarkService.getBookmark(uid, courseId)
            _isCourseBookmarked.value = (bookmark != null)
        }
    }

    fun toggleCourseBookmark(course: Course) {
        viewModelScope.launch {
            if (_isCourseBookmarked.value) {
                courseBookmarkService.deleteBookmark(uid, course.id)
            } else {
                val courseInfo = course.course_info.firstOrNull()
                val description = courseInfo?.course_description ?: ""
                val imageUrl = courseInfo?.places?.firstOrNull()?.imageUrl ?: ""

                courseBookmarkService.addBookmark(
                    uid = uid,
                    courseId = course.id,
                    courseName = course.course_info.firstOrNull()?.course_name ?: course.place_name,
                    courseDescription = description,
                    imageUrl = imageUrl
                )
            }
            _isCourseBookmarked.value = !_isCourseBookmarked.value
        }
    }
}