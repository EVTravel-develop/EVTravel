package com.jeju.evtravel.data.repository

import com.jeju.evtravel.data.model.PlanDto
import com.jeju.evtravel.data.remote.firebase.PlanRemoteDataSource
import com.jeju.evtravel.domain.repository.PlanRepository

/**
 * PlanRepository 인터페이스의 구현체
 * 데이터 소스(현재는 원격 데이터 소스만 사용)와의 통신을 담당합니다.
 */
class PlanRepositoryImpl(
    // 원격 데이터 소스 (기본값으로 초기화됨)
    private val remote: PlanRemoteDataSource = PlanRemoteDataSource()
) : PlanRepository {

    /**
     * 플랜을 저장하는 메서드
     * 원격 데이터 소스를 통해 플랜을 저장합니다.
     *
     * @param plan 저장할 플랜 데이터 (PlanDto)
     * @param onSuccess 저장 성공 시 실행될 콜백 함수
     * @param onFailure 저장 실패 시 실행될 콜백 함수 (예외 전달)
     */
    override fun savePlan(plan: PlanDto, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        // 원격 데이터 소스를 통해 플랜 저장 요청 전달
        remote.savePlan(plan, onSuccess, onFailure)
    }
}