package com.jeju.evtravel.data.remote.dto

data class ChargerApiResponseDto(
    val resultMsg: String,
    val totalCount: Int,
    val items: ChargerItems,
    val pageNo: Int,
    val resultCode: String,
    val numOfRows: Int
)

data class ChargerItems(
    val item: List<ChargerDto>
)