package com.jeju.evtravel.data.model

data class User(
    val uuid: String = "",
    val type: String = "", // "guest" 또는 "kakao"
    val name: String = ""
)

