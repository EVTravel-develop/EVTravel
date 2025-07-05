package com.jeju.evtravel.data.repository

import com.jeju.evtravel.data.model.PlanDto
import com.jeju.evtravel.data.remote.firebase.PlanRemoteDataSource
import com.jeju.evtravel.domain.repository.PlanRepository

class PlanRepositoryImpl(
    private val remote: PlanRemoteDataSource = PlanRemoteDataSource()
) : PlanRepository {

    override fun savePlan(plan: PlanDto, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        remote.savePlan(plan, onSuccess, onFailure)
    }
}