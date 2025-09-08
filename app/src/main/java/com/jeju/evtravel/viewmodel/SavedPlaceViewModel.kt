package com.jeju.evtravel.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.jeju.evtravel.domain.model.PlaceBookmark
import com.jeju.evtravel.service.auth.PlaceBookmarkService
import com.jeju.evtravel.service.auth.FirebaseAuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

// 로그 태그를 위한 상수 정의
private const val TAG = "SavedPlaceViewModel"

class SavedPlaceViewModel(
    private val service: PlaceBookmarkService = PlaceBookmarkService()
) : ViewModel() {

    private val _bookmarks = MutableStateFlow<List<PlaceBookmark>>(emptyList())
    val bookmarks: StateFlow<List<PlaceBookmark>> = _bookmarks

    private var lastSnapshot: DocumentSnapshot? = null
    private var isLoading = false
    private var hasMore = true

    // 현재 로그인한 사용자 UID
    private val uid: String? = FirebaseAuthService.getUserId()

    init {
        // ViewModel 초기화 시 UID 상태 확인
        Log.d(TAG, "ViewModel initialized. Current UID: $uid")
    }

    /** 첫 로드 */
    fun loadBookmarks(limit: Long = 10) {
        // 함수 시작 로그
        Log.d(TAG, "Attempting to load initial bookmarks.")
        if (uid == null) {
            Log.w(TAG, "UID is null. Cannot load bookmarks.")
            return
        }
        if (isLoading) {
            Log.d(TAG, "Already loading. Ignoring new load request.")
            return
        }

        viewModelScope.launch {
            Log.d(TAG, "Starting initial bookmark load for UID: $uid with limit: $limit")
            isLoading = true

            val (data, last) = service.getBookmarksByUid(uid, limit)

            _bookmarks.value = data
            lastSnapshot = last
            hasMore = data.isNotEmpty()
            isLoading = false

            // 로드 결과 로그
            Log.d(TAG, "Initial load complete. Fetched ${data.size} bookmarks. hasMore: $hasMore")
        }
    }


    /** 추가 로드 */
    fun loadMore(limit: Long = 10) {
        // 함수 시작 로그
        Log.d(TAG, "Attempting to load more bookmarks.")
        if (uid == null) {
            Log.w(TAG, "UID is null. Cannot load more bookmarks.")
            return
        }
        if (isLoading) {
            Log.d(TAG, "Already loading. Ignoring new load request.")
            return
        }
        if (!hasMore) {
            Log.d(TAG, "No more data to load. Stopping.")
            return
        }

        viewModelScope.launch {
            Log.d(TAG, "Starting additional bookmark load for UID: $uid with limit: $limit")
            isLoading = true

            val (data, last) = service.getBookmarksByUid(uid, limit, lastSnapshot)

            _bookmarks.value = _bookmarks.value + data
            lastSnapshot = last
            hasMore = data.isNotEmpty()
            isLoading = false

            // 로드 결과 로그
            Log.d(TAG, "Additional load complete. Fetched ${data.size} new bookmarks. Total bookmarks: ${_bookmarks.value.size}. hasMore: $hasMore")
        }
    }
}