package com.jeju.evtravel.data.util

fun mapStatus(code: String): String = when (code) {
    "1" -> "통신 이상"
    "2" -> "사용 가능"
    "3" -> "충전 중"
    "4" -> "운영 중지"
    "5" -> "점검 중"
    else -> "알 수 없음"
}