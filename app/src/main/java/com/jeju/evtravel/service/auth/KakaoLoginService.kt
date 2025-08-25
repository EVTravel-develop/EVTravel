// com/jeju/evtravel/service/auth/KakaoLoginService.kt
package com.jeju.evtravel.service.auth

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.functions.FirebaseFunctions
import com.jeju.evtravel.domain.model.User
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient

object KakaoLoginService {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val functions = FirebaseFunctions.getInstance("us-central1")

    private const val TAG = "KakaoLogin"

    fun kakaoLogin(context: Context, onResult: (Boolean) -> Unit) {
        val doAccountLogin: () -> Unit = {
            Log.d(TAG, "➡️ 카카오 계정 로그인 시도")
            UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
                if (error != null || token == null) {
                    Log.e(TAG, "❌ loginWithKakaoAccount 실패", error)
                    onResult(false); return@loginWithKakaoAccount
                }
                Log.d(TAG, "✅ loginWithKakaoAccount 성공: accessToken=${token.accessToken.take(10)}...")
                proceedWithToken(context, token, onResult)
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            Log.d(TAG, "➡️ 카카오톡 로그인 가능 → loginWithKakaoTalk 시도")
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null || token == null) {
                    Log.e(TAG, "❌ loginWithKakaoTalk 실패 → 계정 로그인 fallback", error)
                    doAccountLogin()
                } else {
                    Log.d(TAG, "✅ loginWithKakaoTalk 성공: accessToken=${token.accessToken.take(10)}...")
                    proceedWithToken(context, token, onResult)
                }
            }
        } else {
            doAccountLogin()
        }
    }

    private fun proceedWithToken(
        context: Context,
        token: OAuthToken,
        onResult: (Boolean) -> Unit
    ) {
        val accessToken = token.accessToken

        if (accessToken.isNullOrBlank()) {
            Log.e(TAG, "❌ accessToken 없음")
            onResult(false); return
        }

        Log.d(TAG, "➡️ proceedWithToken: accessToken=${accessToken.take(10)}...")

        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(TAG, "❌ UserApiClient.me 실패", error)
                onResult(false); return@me
            }

            val account = user?.kakaoAccount
            Log.d(
                TAG,
                "✅ 카카오 프로필 가져오기 성공: id=${user?.id}, nickname=${account?.profile?.nickname}, email=${account?.email}"
            )

            val needsEmailAgreement = account?.emailNeedsAgreement == true

            fun callFunctionAndSignIn(emailFallback: String?, nicknameFallback: String?, imageFallback: String?) {
                val payload = hashMapOf(
                    "accessToken" to accessToken  // infer 타입은 String
                )
                Log.d(TAG, "➡️ Firebase Function kakaoSignIn 호출 직전 payload=$payload")

                functions.getHttpsCallable("kakaoSignIn")
                    .call(payload)
                    .addOnSuccessListener { result ->
                        Log.d(TAG, "✅ kakaoSignIn 함수 원본 result: $result")

                        @Suppress("UNCHECKED_CAST")
                        val data = result.data as? Map<String, Any?>
                        Log.d(TAG, "📦 kakaoSignIn 반환 데이터 = $data")

                        val custom = data?.get("firebaseCustomToken") as? String
                        val nickname = (data?.get("nickname") as? String) ?: nicknameFallback
                        val profileImageUrl = (data?.get("profileImageUrl") as? String) ?: imageFallback
                        val email = (data?.get("email") as? String) ?: emailFallback

                        Log.d(TAG, "✅ 파싱 결과: customToken=${custom?.take(15)}, nickname=$nickname, email=$email")

                        if (custom.isNullOrBlank()) {
                            Log.e(TAG, "❌ firebaseCustomToken 없음")
                            onResult(false); return@addOnSuccessListener
                        }

                        auth.signInWithCustomToken(custom)
                            .addOnSuccessListener {
                                val uid = auth.currentUser?.uid
                                Log.d(TAG, "✅ Firebase Auth 로그인 성공: uid=$uid")

                                if (uid == null) {
                                    Log.e(TAG, "❌ uid 없음")
                                    onResult(false); return@addOnSuccessListener
                                }

                                val newUser = User(
                                    uid = uid,
                                    displayName = nickname ?: "카카오 사용자",
                                    imageUrl = profileImageUrl,
                                    email = email,
                                    createdAt = System.currentTimeMillis()
                                )

                                db.collection("users").document(uid)
                                    .set(newUser, SetOptions.merge())
                                    .addOnSuccessListener {
                                        Log.d(TAG, "✅ Firestore 저장 성공: $newUser")
                                        onResult(true)
                                    }
                                    .addOnFailureListener {
                                        Log.e(TAG, "❌ Firestore 저장 실패", it)
                                        onResult(false)
                                    }
                            }
                            .addOnFailureListener {
                                Log.e(TAG, "❌ Firebase Auth signInWithCustomToken 실패", it)
                                onResult(false)
                            }
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "❌ kakaoSignIn 함수 호출 실패", it)
                        onResult(false)
                    }
            }

            if (needsEmailAgreement) {
                Log.d(TAG, "➡️ 이메일 동의 필요 → 추가 scope 요청")
                UserApiClient.instance.loginWithNewScopes(context, listOf("account_email")) { _, err ->
                    if (err != null) {
                        Log.e(TAG, "❌ loginWithNewScopes 실패", err)
                        onResult(false); return@loginWithNewScopes
                    }
                    UserApiClient.instance.me { user2, err2 ->
                        if (err2 != null) {
                            Log.e(TAG, "❌ UserApiClient.me 재호출 실패", err2)
                            onResult(false); return@me
                        }
                        val acc2 = user2?.kakaoAccount
                        Log.d(TAG, "✅ 이메일 재획득 성공: email=${acc2?.email}")
                        callFunctionAndSignIn(
                            emailFallback = acc2?.email,
                            nicknameFallback = acc2?.profile?.nickname,
                            imageFallback = acc2?.profile?.profileImageUrl
                        )
                    }
                }
            } else {
                callFunctionAndSignIn(
                    emailFallback = account?.email,
                    nicknameFallback = account?.profile?.nickname,
                    imageFallback = account?.profile?.profileImageUrl
                )
            }
        }
    }
}
