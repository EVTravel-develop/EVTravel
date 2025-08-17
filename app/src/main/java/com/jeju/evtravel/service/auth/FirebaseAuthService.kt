// com/jeju/evtravel/service/auth/FirebaseAuthService.kt
package com.jeju.evtravel.service.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.jeju.evtravel.domain.model.User
import kotlinx.coroutines.tasks.await

object FirebaseAuthService {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // 현재 로그인 여부 체크
    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    // UID 가져오기
    fun getUserId(): String? {
        return auth.currentUser?.uid
    }

    // 이름 가져오기 (displayName)
    fun getUserName(): String? {
        return auth.currentUser?.displayName
    }

    // 프로필 이미지 URL 가져오기
    fun getProfileImageUrl(): String? {
        return auth.currentUser?.photoUrl?.toString()
    }

    // 토큰 가져오기 (서버 검증용)
    suspend fun getIdToken(forceRefresh: Boolean = true): String? {
        return auth.currentUser?.getIdToken(forceRefresh)?.await()?.token
    }


}
