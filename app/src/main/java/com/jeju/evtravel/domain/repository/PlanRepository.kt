package com.jeju.evtravel.domain.repository

import com.jeju.evtravel.data.model.PlanDto

/**
 * 플랜 데이터에 대한 저장소 인터페이스
 * 데이터 소스(로컬/원격)와 상호작용하기 위함
 */
interface PlanRepository {
    /**
     * 새로운 플랜을 저장하는 메서드
     * 
     * @param plan 저장할 플랜 데이터 (PlanDto)
     * @param onSuccess 저장 성공 시 실행될 콜백 함수
     * @param onFailure 저장 실패 시 실행될 콜백 함수 (예외 전달)
     */
    fun savePlan(plan: PlanDto, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {})
}