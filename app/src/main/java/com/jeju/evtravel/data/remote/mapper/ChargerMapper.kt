package com.jeju.evtravel.data.remote.mapper

import com.jeju.evtravel.data.remote.dto.ChargerRemoteDto
import com.jeju.evtravel.domain.model.ChargerInfo

fun ChargerRemoteDto.toDomain(): ChargerInfo {
    return ChargerInfo(
        statId = statId ?: "",
        chargerId = chgerId ?: "",
        name = statNm ?: "",
        address = addr ?: "주소 정보 없음",
        usageTime = useTime ?: "",
        provider = busiNm ?: "",
        output = output ?: "",
        chargerType = chgerType ?: "",
        method = method ?: "",
        isFreeParking = parkingFree == "Y",
        contact = busiCall ?: "",
        status = stat ?: "",
        lat = lat?.toDoubleOrNull() ?: throw IllegalArgumentException("위도 변환 실패"),
        lng = lng?.toDoubleOrNull() ?: throw IllegalArgumentException("경도 변환 실패")
    )
}