package com.jeju.evtravel.domain.usecase

import com.jeju.evtravel.data.model.PlanDto
import com.jeju.evtravel.domain.repository.PlanRepository

class SavePlanUseCase(private val repository: PlanRepository) {
    suspend operator fun invoke(plan: PlanDto) {
        repository.savePlan(plan)
    }
}
