package com.jeju.evtravel.domain.repository

import com.jeju.evtravel.data.model.PlanDto

interface PlanRepository {
    fun savePlan(plan: PlanDto, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {})
}