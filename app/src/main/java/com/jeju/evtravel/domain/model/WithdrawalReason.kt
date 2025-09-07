package com.jeju.evtravel.domain.model

data class WithdrawalReason(
    val uid: String = "",
    val reason: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
