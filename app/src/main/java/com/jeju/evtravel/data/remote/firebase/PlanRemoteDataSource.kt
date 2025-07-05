package com.jeju.evtravel.data.remote.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.jeju.evtravel.data.model.PlanDto

class PlanRemoteDataSource(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun savePlan(plan: PlanDto, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        val planWithTimestamps = plan.copy(
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )

        db.collection("plans")
            .add(planWithTimestamps)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}
