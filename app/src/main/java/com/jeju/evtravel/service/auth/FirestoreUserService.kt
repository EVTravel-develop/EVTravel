package com.jeju.evtravel.service.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestore
import com.jeju.evtravel.domain.model.User
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume // ⭐️ 이 줄을 추가합니다.

object FirestoreUserService {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun getUserFromFirestore(onResult: (User?) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.d("FirestoreUserService", "❌ 현재 로그인된 유저 없음")
            onResult(null)
            return
        }

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                    Log.d("FirestoreUserService", "✅ Firestore 유저 불러옴 → $user")
                    onResult(user)
                } else {
                    Log.d("FirestoreUserService", "⚠️ Firestore에 유저 문서 없음")
                    onResult(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreUserService", "❌ Firestore 유저 불러오기 실패", e)
                onResult(null)
            }
    }

    fun updateDisplayName(newName: String, onResult: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.d("FirestoreUserService", "❌ 현재 로그인된 유저 없음 → 닉네임 업데이트 불가")
            onResult(false)
            return
        }

        db.collection("users")
            .document(uid)
            .update("displayName", newName)
            .addOnSuccessListener {
                Log.d("FirestoreUserService", "✅ 닉네임 변경 성공 → $newName")
                onResult(true)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreUserService", "❌ 닉네임 변경 실패", e)
                onResult(false)
            }
    }

    // ✅ 콜백 기반 함수를 suspend 함수로 변경
    suspend fun deleteUserAccount(): Boolean = suspendCancellableCoroutine { continuation ->
        val uid = auth.currentUser?.uid
        val user = auth.currentUser

        // ⭐️ 코루틴이 취소되었을 때의 동작을 정의
        // Firebase 작업은 취소 기능이 없으므로 로그만 남깁니다.
        continuation.invokeOnCancellation {
            Log.d("FirestoreUserService", "계정 삭제 코루틴이 취소되었습니다.")
        }

        if (uid == null || user == null) {
            Log.d("FirestoreUserService", "❌ 현재 로그인된 유저 없음 → 탈퇴 불가")
            continuation.resume(false)
            return@suspendCancellableCoroutine
        }

        db.collection("users")
            .document(uid)
            .delete()
            .addOnSuccessListener {
                Log.d("FirestoreUserService", "✅ Firestore 유저 문서 삭제 성공")

                user.delete()
                    .addOnSuccessListener {
                        Log.d("FirestoreUserService", "✅ FirebaseAuth 계정 삭제 성공")
                        continuation.resume(true)
                    }
                    .addOnFailureListener { e ->
                        if (e is FirebaseAuthRecentLoginRequiredException) {
                            Log.w("FirestoreUserService", "⚠️ 계정 삭제를 위해 재로그인이 필요합니다.", e)
                        } else {
                            Log.e("FirestoreUserService", "❌ FirebaseAuth 계정 삭제 실패", e)
                        }
                        continuation.resume(false)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreUserService", "❌ Firestore 유저 문서 삭제 실패", e)
                continuation.resume(false)
            }
    }

    // 여행 계획 삭제
    suspend fun deletePlansByUid(uid: String): Boolean {
        val querySnapshot = db.collection("plans").whereEqualTo("userId", uid).get().await()
        return try {
            val batch = db.batch()
            for (document in querySnapshot.documents) {
                batch.delete(document.reference)
            }
            batch.commit().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
