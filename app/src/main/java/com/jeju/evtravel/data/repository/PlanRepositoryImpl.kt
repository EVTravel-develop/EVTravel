package com.jeju.evtravel.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.jeju.evtravel.data.model.PlanDto
import com.jeju.evtravel.domain.repository.PlanRepository
import kotlinx.coroutines.tasks.await
import java.util.*

class PlanRepositoryImpl : PlanRepository {

    private val db = FirebaseFirestore.getInstance()
    private val plansRef = db.collection("plans")

    override suspend fun savePlan(plan: PlanDto) {
        val id = UUID.randomUUID().toString()
        plansRef.document(id).set(plan.copy(id = id)).await()
    }

    override suspend fun getPlans(): List<PlanDto> {
        return plansRef.get().await().toObjects(PlanDto::class.java)
    }
}
