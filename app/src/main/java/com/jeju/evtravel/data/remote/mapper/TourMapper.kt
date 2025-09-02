package com.jeju.evtravel.data.remote.mapper

import com.jeju.evtravel.data.remote.dto.TourItem
import com.jeju.evtravel.domain.model.TourPlace

fun TourItem.toDomain(): TourPlace =
    TourPlace(
        title = title,
        address = addr1,
        x = mapx,
        y = mapy,
        dist = dist,
        contentId = contentid,
        contentTypeId = contenttypeid,
        firstImage = firstimage,
        firstImage2 = firstimage2,
        areaCode = areacode,
        sigunguCode = sigungucode,
        tel = tel,
        cat1 = cat1,
        cat2 = cat2,
        cat3 = cat3,
        lclsSystm1 = lclsSystm1,
        lclsSystm2 = lclsSystm2,
        lclsSystm3 = lclsSystm3,
        zipCode = zipcode,
    )