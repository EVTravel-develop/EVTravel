package com.jeju.evtravel.domain.usecase

import com.jeju.evtravel.data.model.PlanDto
import com.jeju.evtravel.domain.repository.PlanRepository

class SavePlanUseCase(
    private val repository: PlanRepository
) {
    operator fun invoke(plan: PlanDto, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        repository.savePlan(plan, onSuccess, onFailure)
    }
}
