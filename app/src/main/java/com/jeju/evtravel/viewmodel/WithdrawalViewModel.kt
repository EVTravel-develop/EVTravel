package com.jeju.evtravel.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeju.evtravel.domain.model.User
import com.jeju.evtravel.service.auth.CourseBookmarkService
import com.jeju.evtravel.service.auth.FirebaseAuthService
import com.jeju.evtravel.service.auth.FirestoreUserService
import com.jeju.evtravel.service.auth.PlaceBookmarkService
import com.jeju.evtravel.service.auth.WithdrawalService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WithdrawalViewModel : ViewModel() {

    // Service 클래스들을 직접 생성합니다.
    private val firestoreUserService = FirestoreUserService
    private val courseBookmarkService = CourseBookmarkService()
    private val placeBookmarkService = PlaceBookmarkService()
    private val withdrawalService = WithdrawalService

    private val _user = mutableStateOf<User?>(null)
    val user: State<User?> = _user

    private val _withdrawSuccess = mutableStateOf<Boolean?>(null)
    val withdrawSuccess: State<Boolean?> = _withdrawSuccess

    // ⭐️ 로딩 상태를 위한 StateFlow
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    init {
        loadUser()
    }

    private fun loadUser() {
        firestoreUserService.getUserFromFirestore { result ->
            _user.value = result
        }
    }

    fun withdrawAccount(reason: String) {
        val uid = FirebaseAuthService.getUserId()

        if (uid == null) {
            _withdrawSuccess.value = false
            return
        }

        // ⭐️ viewModelScope를 사용해 모든 작업을 코루틴 내에서 실행합니다.
        viewModelScope.launch {
            // ⭐️ 로딩 시작
            _isProcessing.value = true

            try {
                // 1. 탈퇴 사유 저장
                val reasonSaved = withdrawalService.saveWithdrawalReason(reason)
                if (!reasonSaved) {
                    _withdrawSuccess.value = false
                    return@launch
                }

                // 2. 모든 데이터를 순차적으로 삭제합니다.
                val courseDeleted = courseBookmarkService.deleteCourseBookmarksByUid(uid)
                if (!courseDeleted) {
                    _withdrawSuccess.value = false
                    return@launch
                }

                val placeDeleted = placeBookmarkService.deletePlaceBookmarksByUid(uid)
                if (!placeDeleted) {
                    _withdrawSuccess.value = false
                    return@launch
                }

                val plansDeleted = firestoreUserService.deletePlansByUid(uid)
                if (!plansDeleted) {
                    _withdrawSuccess.value = false
                    return@launch
                }

                // 3. 모든 데이터 삭제가 성공하면, 최종적으로 사용자 계정을 삭제합니다.
                val accountDeleted = firestoreUserService.deleteUserAccount()
                _withdrawSuccess.value = accountDeleted

            } catch (e: Exception) {
                // 예외 발생 시 실패로 처리
                _withdrawSuccess.value = false
            } finally {
                // ⭐️ 작업 완료 시 로딩 상태 변경
                _isProcessing.value = false
            }
        }
    }
}