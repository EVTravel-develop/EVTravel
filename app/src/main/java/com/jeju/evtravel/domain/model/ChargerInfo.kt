package com.jeju.evtravel.domain.model

data class ChargerInfo(
    val statId: String,
    val chargerId: String,
    val name: String,
    val address: String,
    val usageTime: String,
    val provider: String,
    val output: String,
    val chargerType: String,
    val method: String,
    val isFreeParking: Boolean,
    val contact: String,
    val status: String,
    val lat: Double?,
    val lng: Double?
)
