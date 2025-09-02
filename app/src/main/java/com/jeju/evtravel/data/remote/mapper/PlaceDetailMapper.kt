package com.jeju.evtravel.data.remote.mapper

import com.jeju.evtravel.data.remote.dto.TourInfoItem
import com.jeju.evtravel.domain.model.TourPlaceDetail

fun TourInfoItem.toDomain(): TourPlaceDetail =
    TourPlaceDetail(
        contentId = contentId ?: "",
        title = title,
        addr1 = addr1,
        addr2 = addr2,
        tel = tel,
        overview = overview,
        firstImage = firstImage,
        firstImage2 = firstImage2,
        longitude = mapX ?: 0.0,
        latitude = mapY ?: 0.0
    )