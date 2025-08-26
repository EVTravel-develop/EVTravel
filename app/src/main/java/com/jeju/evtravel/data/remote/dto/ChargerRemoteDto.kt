package com.jeju.evtravel.data.remote.dto

data class ChargerRemoteDto(
    val statId: String? = null,
    val chgerId: String? = null,
    val statNm: String? = null,
    val addr: String? = null,
    val useTime: String? = null,
    val busiNm: String? = null,
    val output: String? = null,
    val chgerType: String? = null,
    val method: String? = null,
    val parkingFree: String? = null,  // "Y"/"N"/null
    val busiCall: String? = null,
    val stat: String? = null,
    val lat: String? = null,
    val lng: String? = null
)
