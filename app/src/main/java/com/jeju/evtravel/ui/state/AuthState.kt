package com.jeju.evtravel.auth

/**
 * Firebase Authentication 상태
 */
sealed class AuthState {

    /**
     * 로그인 상태를 확인하는 중
     */
    object Loading : AuthState()

    /**
     * 로그인이 되지 않은 상태
     */
    object Unauthenticated : AuthState()

    /**
     * 로그인이 된 상태
     * @property uid 사용자 UID
     */
    data class Authenticated(val uid: String) : AuthState()

    /**
     * 에러가 발생한 상태
     * @property message 에러 메시지
     */
    data class Error(val message: String) : AuthState()
}