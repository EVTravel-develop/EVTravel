package com.jeju.evtravel.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.jeju.evtravel.domain.model.CourseBookmark
import com.jeju.evtravel.service.auth.CourseBookmarkService
import com.jeju.evtravel.service.auth.FirebaseAuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log // Log 클래스 임포트

// REMEMBER TO USE HILT'S @HiltViewModel and @Inject IN A REAL APP
class SavedCourseViewModel(
    private val service: CourseBookmarkService = CourseBookmarkService()
) : ViewModel() {

    private val _bookmarks = MutableStateFlow<List<CourseBookmark>>(emptyList())
    val bookmarks: StateFlow<List<CourseBookmark>> = _bookmarks

    private var lastSnapshot: DocumentSnapshot? = null
    private var isLoading = false
    private var hasMore = true

    private val uid: String? = FirebaseAuthService.getUserId()

    /** 첫 로드 */
    fun loadBookmarks(limit: Long = 10) {
        if (uid == null || isLoading) return
        viewModelScope.launch {
            isLoading = true
            val (data, last) = service.getBookmarksByUid(uid, limit)

            // ✅ 로그 추가: 서비스에서 받은 원본 데이터 확인
            Log.d("SavedCourseViewModel", "Raw data from service: ${data.size} items")
            data.forEach { bookmark ->
                Log.d("SavedCourseViewModel", "Raw Bookmark: name=${bookmark.course_name}, desc=${bookmark.course_description}")
            }

            val processedData = processBookmarks(data)
            _bookmarks.value = processedData

            // ✅ 로그 추가: UI로 전달될 최종 데이터 확인
            Log.d("SavedCourseViewModel", "Processed data passed to UI: ${_bookmarks.value.size} items")
            _bookmarks.value.forEach { bookmark ->
                Log.d("SavedCourseViewModel", "Final Bookmark: name=${bookmark.course_name}, desc=${bookmark.course_description}")
            }

            lastSnapshot = last
            hasMore = data.isNotEmpty()
            isLoading = false
        }
    }

    /** 추가 로드 */
    fun loadMore(limit: Long = 10) {
        if (uid == null || isLoading || !hasMore) return
        viewModelScope.launch {
            isLoading = true
            val (data, last) = service.getBookmarksByUid(uid, limit, lastSnapshot)

            // ✅ 로그 추가: 서비스에서 받은 원본 데이터 확인
            Log.d("SavedCourseViewModel", "Raw data from service (more): ${data.size} items")
            data.forEach { bookmark ->
                Log.d("SavedCourseViewModel", "Raw Bookmark (more): name=${bookmark.course_name}, desc=${bookmark.course_description}")
            }

            val processedData = processBookmarks(data)
            _bookmarks.value = _bookmarks.value + processedData

            // ✅ 로그 추가: UI로 전달될 최종 데이터 확인
            Log.d("SavedCourseViewModel", "Processed data passed to UI (more): ${_bookmarks.value.size} items")
            _bookmarks.value.forEach { bookmark ->
                Log.d("SavedCourseViewModel", "Final Bookmark (more): name=${bookmark.course_name}, desc=${bookmark.course_description}")
            }

            lastSnapshot = last
            hasMore = data.isNotEmpty()
            isLoading = false
        }
    }

    private fun processBookmarks(bookmarks: List<CourseBookmark>): List<CourseBookmark> {
        return bookmarks.map { bookmark ->
            val processedName = if (bookmark.course_name.isEmpty()) "이름 없음" else bookmark.course_name
            val processedDescription = if (bookmark.course_description.isEmpty()) "설명 없음" else bookmark.course_description

            bookmark.copy(
                course_name = processedName,
                course_description = processedDescription
            )
        }
    }
}