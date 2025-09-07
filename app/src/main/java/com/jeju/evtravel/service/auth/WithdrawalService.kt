package com.jeju.evtravel.service.auth

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore // db는 계속 사용해야 하므로 남겨둡니다.
import com.jeju.evtravel.domain.model.WithdrawalReason

object WithdrawalService {

    private val db = FirebaseFirestore.getInstance()

    fun saveWithdrawalReason(reason: String, onResult: (Boolean) -> Unit) {
        // ✅ FirestoreUserService를 통해 현재 유저 정보를 가져옵니다.
        FirestoreUserService.getUserFromFirestore { user ->
            if (user == null || user.uid.isEmpty()) {
                Log.d("WithdrawalService", "❌ 유저 정보 없음 → 탈퇴 사유 저장 불가")
                onResult(false)
                return@getUserFromFirestore
            }

            // ✅ 가져온 user 객체에서 uid를 사용합니다.
            val withdrawalReason = WithdrawalReason(
                uid = user.uid,
                reason = reason
            )

            db.collection("withdrawals")
                .add(withdrawalReason)
                .addOnSuccessListener {
                    Log.d("WithdrawalService", "✅ 탈퇴 사유 저장 성공 → $withdrawalReason")
                    onResult(true)
                }
                .addOnFailureListener { e ->
                    Log.e("WithdrawalService", "❌ 탈퇴 사유 저장 실패", e)
                    onResult(false)
                }
        }
    }
}