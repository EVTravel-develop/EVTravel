package com.jeju.evtravel.data.remote.dto

data class ChargerDto(
    val statId: String,
    val chgerId: String,
    val statNm: String,
    val addr: String,
    val useTime: String,
    val busiNm: String,
    val output: String,
    val chgerType: String,
    val method: String,
    val parkingFree: String,
    val busiCall: String,
    val stat: String,
    val lat: String,
    val lng: String
)
