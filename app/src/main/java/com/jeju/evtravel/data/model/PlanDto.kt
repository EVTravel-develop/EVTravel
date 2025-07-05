package com.jeju.evtravel.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class PlanDto(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val destination: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val days: List<String> = emptyList(),
    val userId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)