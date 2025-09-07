package com.jeju.evtravel.service.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object WithdrawalService {

    private val db = FirebaseFirestore.getInstance()

    fun saveWithdrawalReason(reason: String, onResult: (Boolean) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrEmpty()) {
            Log.d("WithdrawalService", "❌ 유저 정보 없음 → 탈퇴 사유 저장 불가")
            onResult(false)
            return
        }

        // WithdrawalReason 데이터 클래스 대신 Map을 직접 사용해 유연성을 높이고 서버 타임스탬프를 적용합니다.
        val payload = mapOf(
            "uid" to uid,
            "reason" to reason.trim().take(500), // 입력값 공백 제거 및 글자 수 제한 (500자)
            "timestamp" to FieldValue.serverTimestamp() // ✅ 클라이언트 시간이 아닌 서버 시간 사용
        )

        db.collection("withdrawals")
            .add(payload)
            .addOnSuccessListener {
                Log.d("WithdrawalService", "✅ 탈퇴 사유 저장 성공")
                onResult(true)
            }
            .addOnFailureListener { e ->
                Log.e("WithdrawalService", "❌ 탈퇴 사유 저장 실패", e)
                onResult(false)
            }
    }
}