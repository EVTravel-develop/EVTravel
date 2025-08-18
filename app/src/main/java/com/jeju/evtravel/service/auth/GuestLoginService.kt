package com.jeju.evtravel.data.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jeju.evtravel.domain.model.User

object GuestLoginService {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun guestLogin(onResult: (Boolean) -> Unit) {
        auth.signInAnonymously()
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    val uid = user.uid

                    val baseNickname = "제주도 여행자_"
                    val number = (1..9999).random()
                    val nickname = "$baseNickname$number"

                    // ✅ User 모델 생성
                    val newUser = User(
                        uid = uid,
                        displayName = nickname,
                        imageUrl = null,
                        email = null,
                        createdAt = System.currentTimeMillis(),
                    )

                    // Firestore에 저장
                    db.collection("users")
                        .document(uid)
                        .set(newUser)
                        .addOnSuccessListener { onResult(true) }
                        .addOnFailureListener { onResult(false) }
                } else {
                    onResult(false)
                }
            }
            .addOnFailureListener {
                onResult(false)
            }
    }
}
