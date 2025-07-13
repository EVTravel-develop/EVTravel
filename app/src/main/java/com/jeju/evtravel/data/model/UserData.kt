package com.jeju.evtravel.data.model

import com.google.firebase.Timestamp

/**
 * 사용자 데이터 모델
 *
 * @property uid UUID firebase auth uid
 * @property name 사용자 이름 (게스트 또는 카카오 이름)
 * @property email 이메일 (카카오 로그인 시 사용)
 * @property profileImageUrl 프로필 이미지 URL (카카오 로그인 시 사용)
 * @property isRegistered 정회원 여부 (true면 카카오 연동 완료)
 * @property createdAt 계정 생성 시각
 */
data class UserData(
    val uid: String = "",
    val name: String = "",
    val email: String? = null,
    val profileImageUrl: String? = null,
    val isRegistered: Boolean = false,
    val createdAt: Timestamp = Timestamp.now()
)