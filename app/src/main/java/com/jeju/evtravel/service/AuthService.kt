package com.jeju.evtravel.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.jeju.evtravel.data.model.UserData
import com.jeju.evtravel.utils.UUIDManager
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient

class AuthService {

    fun isLoggedIn(context: Context): Boolean {
        return !UUIDManager.get(context).isNullOrEmpty()
    }

    fun guestLogin(context: Context, onSuccess: (String) -> Unit) {
        val uuid = UUIDManager.create(context)
        // Firestore에도 저장(최초 1회만)
        val db = Firebase.firestore
        val userData = UserData(
            uid = uuid,
            name = "게스트-${uuid.take(6)}",
            isRegistered = false,
            createdAt = Timestamp.now()
        )
        db.collection("users").document(uuid)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    db.collection("users").document(uuid).set(userData)
                }
            }
        onSuccess(uuid)
    }

    /**
     * 카카오톡 로그인 (토큰 → 카카오 userId → Firestore 동기화)
     */
    fun kakaoLoginWithKakaoTalk(
        activity: Activity,
        context: Context,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable?) -> Unit = {}
    ) {
        UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
            if (error != null) {
                // 카카오톡 미설치 등 특정 상황 처리
                if (error is ClientError && error.reason == ClientErrorCause.NotSupported) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.kakao.talk"))
                    activity.startActivity(intent)
                } else {
                    onFailure(error)
                }
            } else if (token != null) {
                // 카카오 로그인 성공 → 사용자 정보 가져오기
                UserApiClient.instance.me { user, userError ->
                    if (userError != null || user == null) {
                        onFailure(userError)
                        return@me
                    }
                    val kakaoId = user.id?.toString() ?: return@me
                    // UUIDManager에 업데이트
                    UUIDManager.update(context, kakaoId)
                    val name = user.kakaoAccount?.profile?.nickname ?: "카카오 사용자"
                    val email = user.kakaoAccount?.email
                    val profileUrl = user.kakaoAccount?.profile?.profileImageUrl

                    val db = Firebase.firestore
                    val userDoc = db.collection("users").document(kakaoId)
                    userDoc.get().addOnSuccessListener { doc ->
                        if (!doc.exists()) {
                            // Firestore에 최초 생성
                            val userData = UserData(
                                uid = kakaoId,
                                name = name,
                                email = email,
                                profileImageUrl = profileUrl,
                                isRegistered = true,
                                createdAt = Timestamp.now()
                            )
                            userDoc.set(userData)
                        }
                        onSuccess(kakaoId)
                    }.addOnFailureListener {
                        onFailure(it)
                    }
                }
            }
        }
    }
}
