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
            _bookmarks.value = data
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
            _bookmarks.value = _bookmarks.value + data
            lastSnapshot = last
            hasMore = data.isNotEmpty()
            isLoading = false
        }
    }
}
