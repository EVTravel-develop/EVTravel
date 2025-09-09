package com.jeju.evtravel.ui.detail.course

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase
import com.jeju.evtravel.domain.model.Course
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TAG = "CourseViewModel"

class CourseViewModel  : ViewModel() {

    // Hilt 대신 직접 Firestore 인스턴스를 가져옵니다.
    private val firestore: FirebaseFirestore = Firebase.firestore

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
            firestore.collection("course")
                .whereEqualTo("id", placeId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    Log.d(TAG, "Found ${querySnapshot.size()} documents.")
                    val course = querySnapshot.toObjects<Course>().firstOrNull()
                    if (course != null) {
                        Log.d(TAG, "Successfully converted course: ${course.course_info.firstOrNull()?.course_name}")
                        _state.value = _state.value.copy(loading = false, data = course)
                    } else {
                        Log.d(TAG, "No course found for this placeId.")
                        _state.value = _state.value.copy(loading = false, data = null)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to fetch course: ${e.message}", e)
                    _state.value = _state.value.copy(loading = false, error = e.message)
                }
        }
    }
}

data class CourseUiState(
    val loading: Boolean = false,
    val data: Course? = null,
    val error: String? = null
)