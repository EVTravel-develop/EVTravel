package com.jeju.evtravel.data.remote.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.jeju.evtravel.data.model.PlanDto

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
}
