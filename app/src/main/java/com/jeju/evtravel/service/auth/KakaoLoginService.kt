// com/jeju/evtravel/service/auth/KakaoLoginService.kt
package com.jeju.evtravel.service.auth


import android.content.Context
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

    private val functions = FirebaseFunctions.getInstance("asia-northeast3")

    fun kakaoLogin(context: Context, onResult: (Boolean) -> Unit) {
        val doAccountLogin: () -> Unit = {
            UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
                if (error != null || token == null) { onResult(false); return@loginWithKakaoAccount }
                proceedWithToken(context, token, onResult)
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null || token == null) doAccountLogin()
                else proceedWithToken(context, token, onResult)
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
        val idToken = token.idToken // OIDC 활성화 되어 있으면 채워짐

        if (accessToken.isNullOrBlank()) { onResult(false); return }

        UserApiClient.instance.me { user, error ->
            if (error != null) { onResult(false); return@me }

            val account = user?.kakaoAccount
            val needsEmailAgreement = account?.emailNeedsAgreement == true

            fun callFunctionAndSignIn(emailFallback: String?, nicknameFallback: String?, imageFallback: String?) {
                val payload = hashMapOf(
                    "accessToken" to accessToken,
                    "idToken" to (idToken ?: "")
                )

                functions.getHttpsCallable("kakaoSignIn")
                    .call(payload)
                    .addOnSuccessListener { result ->
                        @Suppress("UNCHECKED_CAST")
                        val data = result.data as? Map<String, Any?>
                        val custom = data?.get("firebaseCustomToken") as? String
                        val nickname = (data?.get("nickname") as? String) ?: nicknameFallback
                        val profileImageUrl = (data?.get("profileImageUrl") as? String) ?: imageFallback
                        val email = (data?.get("email") as? String) ?: emailFallback

                        if (custom.isNullOrBlank()) { onResult(false); return@addOnSuccessListener }

                        auth.signInWithCustomToken(custom)
                            .addOnSuccessListener {
                                val uid = auth.currentUser?.uid ?: run { onResult(false); return@addOnSuccessListener }
                                val newUser = User(
                                    uid = uid,
                                    displayName = nickname ?: "카카오 사용자",
                                    imageUrl = profileImageUrl,
                                    email = email,
                                    createdAt = System.currentTimeMillis()
                                )
                                db.collection("users").document(uid)
                                    .set(newUser, SetOptions.merge())
                                    .addOnSuccessListener { onResult(true) }
                                    .addOnFailureListener { onResult(false) }
                            }
                            .addOnFailureListener { onResult(false) }
                    }
                    .addOnFailureListener { onResult(false) }
            }

            if (needsEmailAgreement) {
                // ✅ 변경: 추가 동의는 loginWithNewScopes 사용
                UserApiClient.instance.loginWithNewScopes(context, listOf("account_email")) { _, err ->
                    if (err != null) { onResult(false); return@loginWithNewScopes }
                    UserApiClient.instance.me { user2, err2 ->
                        if (err2 != null) { onResult(false); return@me }
                        val acc2 = user2?.kakaoAccount
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
