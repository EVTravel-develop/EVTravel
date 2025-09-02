package com.jeju.evtravel.data.remote.firebase

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.jeju.evtravel.data.model.PlanDto
import kotlinx.coroutines.tasks.await

/**
 * Firebase Firestore와 직접적으로 통신하는 데이터 소스 클래스
 * 플랜 데이터의 원격 저장소 작업을 담당합니다.
 */
class PlanRemoteDataSource(
    // Firebase Firestore 인스턴스 (기본값으로 초기화됨)
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /**
     * 새로운 플랜을 Firestore에 저장하는 메서드
     *
     * @param plan 저장할 플랜 데이터 (PlanDto)
     * @param onSuccess 저장 성공 시 실행될 콜백 함수
     * @param onFailure 저장 실패 시 실행될 콜백 함수 (예외 전달)
     */
    private val TAG = "PlannerDebug"
    
    fun savePlan(plan: PlanDto, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        // 현재 시간으로 생성/수정 시간을 설정한 새로운 플랜 객체 생성
        val planWithTimestamps = plan.copy(
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )

        // Firestore의 'plans' 컬렉션에 문서 추가
        db.collection("plans")
            .add(planWithTimestamps)
            .addOnSuccessListener { onSuccess() }  // 성공 시 콜백 실행
            .addOnFailureListener { onFailure(it) } // 실패 시 예외와 함께 콜백 실행
    }

    /**
     * 사용자 ID에 해당하는 플랜 목록을 Firestore에서 가져오는 메서드
     *
     * @param userId 사용자 ID
     * @return 플랜 목록 (List<PlanDto>)
     */
    suspend fun fetchPlans(userId: String): List<PlanDto> {
        return try {
            val snapshot = db.collection("plans")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(PlanDto::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 기존 플랜을 Firestore에서 업데이트하는 메서드
     *
     * @param plan 업데이트할 플랜 데이터
     */
    suspend fun updatePlan(plan: PlanDto) {
        val planId = plan.id.ifEmpty {
            Log.e(TAG, "PlanRemoteDataSource: Update failed because Plan ID is empty.")
            throw IllegalArgumentException("업데이트할 Plan의 ID가 없습니다.")
        }
        // 수정 시간을 현재 시간으로 설정
        Log.d(TAG, "PlanRemoteDataSource: Attempting to update planId '$planId' in Firestore.")
        val planWithTimestamp = plan.copy(updatedAt = Timestamp.now())
        
        try {
            db.collection("plans").document(planId)
                .set(planWithTimestamp)
                .await()
            Log.i(TAG, "PlanRemoteDataSource: Successfully updated planId '$planId' in Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "PlanRemoteDataSource: Firestore update FAILED for planId '$planId'.", e)
            throw e
        }
    }

    /**
     * 특정 플랜을 Firestore에서 삭제하는 메서드
     *
     * @param planId 삭제할 플랜의 ID
     * @param onComplete 삭제 완료 시 실행될 콜백 함수
     * @param onFailure 삭제 실패 시 실행될 콜백 함수 (예외 전달)
     */
    suspend fun deletePlan(planId: String) {
        try {
            db.collection("plans").document(planId)
                .delete()
                .await()
        } catch (e: Exception) {
            throw e
        }
    }
}