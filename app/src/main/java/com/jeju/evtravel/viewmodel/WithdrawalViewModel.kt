package com.jeju.evtravel.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.jeju.evtravel.domain.model.User
import com.jeju.evtravel.service.auth.FirestoreUserService
import com.jeju.evtravel.service.auth.WithdrawalService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WithdrawalViewModel @Inject constructor() : ViewModel() {

    private val _user = mutableStateOf<User?>(null)
    val user: State<User?> = _user

    private val _withdrawSuccess = mutableStateOf<Boolean?>(null)
    val withdrawSuccess: State<Boolean?> = _withdrawSuccess

    init {
        loadUser()
    }

    private fun loadUser() {
        FirestoreUserService.getUserFromFirestore { result ->
            _user.value = result
        }
    }

    fun withdrawAccount(reason: String) {
        // 1. 탈퇴 사유 먼저 저장
        WithdrawalService.saveWithdrawalReason(reason) { reasonSaved ->
            if (reasonSaved) {
                // 2. 사유 저장이 성공하면, 계정 삭제 진행
                FirestoreUserService.deleteUserAccount { accountDeleted ->
                    _withdrawSuccess.value = accountDeleted
                }
            } else {
                // 사유 저장 실패 시, 탈퇴 처리 실패로 간주
                _withdrawSuccess.value = false
            }
        }
    }
}