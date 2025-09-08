package com.jeju.evtravel.service.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException // ⭐️ 이 줄을 추가해야 합니다.

object WithdrawalService {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // ✅ 콜백 기반 함수를 suspend 함수로 변경
    suspend fun saveWithdrawalReason(reason: String): Boolean = suspendCancellableCoroutine { continuation ->
        val uid = auth.currentUser?.uid
        if (uid.isNullOrEmpty()) {
            Log.d("WithdrawalService", "❌ 유저 정보 없음 → 탈퇴 사유 저장 불가")
            continuation.resume(false)
            return@suspendCancellableCoroutine
        }

        // ⭐️ 코루틴이 취소되었을 때의 동작을 정의합니다.
        // Firebase 작업은 취소 기능이 없으므로 로그만 남깁니다.
        continuation.invokeOnCancellation {
            Log.d("WithdrawalService", "탈퇴 사유 저장 코루틴이 취소되었습니다.")
        }

        val payload = mapOf(
            "uid" to uid,
            "reason" to reason.trim().take(500),
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("withdrawals")
            .add(payload)
            .addOnSuccessListener {
                Log.d("WithdrawalService", "✅ 탈퇴 사유 저장 성공")
                continuation.resume(true)
            }
            .addOnFailureListener { e ->
                Log.e("WithdrawalService", "❌ 탈퇴 사유 저장 실패", e)
                continuation.resume(false)
            }
    }
}