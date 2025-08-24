package com.jeju.evtravel.data.remote.mapper

import com.jeju.evtravel.data.remote.dto.ChargerRemoteDto
import com.jeju.evtravel.domain.model.ChargerInfo

fun ChargerRemoteDto.toDomain(): ChargerInfo {
    return ChargerInfo(
        statId = statId ?: "",
        chargerId = chgerId ?: "",
        name = statNm ?: "",
        address = addr ?: "",
        usageTime = useTime ?: "",
        provider = busiNm ?: "",
        output = output ?: "",
        chargerType = chgerType ?: "",
        method = method ?: "",
        isFreeParking = parkingFree == "Y",
        contact = busiCall ?: "",
        status = stat ?: "",
        lat = lat?.toDoubleOrNull() ?: 0.0,
        lng = lng?.toDoubleOrNull() ?: 0.0
    )
}