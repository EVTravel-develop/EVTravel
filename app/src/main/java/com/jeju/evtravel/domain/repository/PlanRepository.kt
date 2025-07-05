package com.jeju.evtravel.domain.repository

import com.jeju.evtravel.data.model.PlanDto

interface PlanRepository {
    suspend fun savePlan(plan: PlanDto)
    suspend fun getPlans(): List<PlanDto>
}
