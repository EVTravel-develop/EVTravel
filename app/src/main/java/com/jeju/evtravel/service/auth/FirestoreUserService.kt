package com.jeju.evtravel.service.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestore
import com.jeju.evtravel.domain.model.User

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

    fun deleteUserAccount(onResult: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid
        val user = auth.currentUser
        if (uid == null || user == null) {
            Log.d("FirestoreUserService", "❌ 현재 로그인된 유저 없음 → 탈퇴 불가")
            onResult(false)
            return
        }

        db.collection("users")
            .document(uid)
            .delete()
            .addOnSuccessListener {
                Log.d("FirestoreUserService", "✅ Firestore 유저 문서 삭제 성공")

                user.delete()
                    .addOnSuccessListener {
                        Log.d("FirestoreUserService", "✅ FirebaseAuth 계정 삭제 성공")
                        onResult(true)
                    }
                    .addOnFailureListener { e ->
                        // ✅ 재로그인이 필요하다는 특정 에러를 잡아서 로그를 남깁니다.
                        if (e is FirebaseAuthRecentLoginRequiredException) {
                            Log.w("FirestoreUserService", "⚠️ 계정 삭제를 위해 재로그인이 필요합니다.", e)
                            // TODO: ViewModel에 재로그인이 필요하다는 상태를 전달하여 UI에서 재로그인 화면을 띄워주는 처리가 필요합니다.
                        } else {
                            Log.e("FirestoreUserService", "❌ FirebaseAuth 계정 삭제 실패", e)
                        }
                        onResult(false)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreUserService", "❌ Firestore 유저 문서 삭제 실패", e)
                onResult(false)
            }
    }
}
