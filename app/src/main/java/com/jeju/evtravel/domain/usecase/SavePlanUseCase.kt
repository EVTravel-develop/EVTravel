package com.jeju.evtravel.domain.usecase

import com.jeju.evtravel.data.model.PlanDto
import com.jeju.evtravel.domain.repository.PlanRepository

/**
 * 플랜 저장을 담당하는 유스케이스 클래스
 * 도메인 로직을 캡슐화하고, 비즈니스 규칙을 적용합니다.
 */
class SavePlanUseCase(
    // 플랜 저장을 위한 리포지토리 의존성 주입
    private val repository: PlanRepository
) {
    /**
     * 플랜을 저장하는 연산자 함수
     * 
     * @param plan 저장할 플랜 데이터 (PlanDto)
     * @param onSuccess 저장 성공 시 실행될 콜백 함수 (기본값: 빈 함수)
     * @param onFailure 저장 실패 시 실행될 콜백 함수 (예외 전달, 기본값: 예외 무시)
     */
    operator fun invoke(plan: PlanDto, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        // 리포지토리를 통해 플랜 저장 로직 실행
        repository.savePlan(plan, onSuccess, onFailure)
    }
}
