package com.jeju.evtravel.data.repository

import com.jeju.evtravel.data.remote.api.TourApi
import com.jeju.evtravel.data.remote.mapper.toDomain
import com.jeju.evtravel.domain.model.TourPlace
import com.jeju.evtravel.domain.repository.TourPlaceRepository

class TourPlaceRepositoryImpl(
    private val api: TourApi,
    private val tourApiKey: String
) : TourPlaceRepository {
    override suspend fun getNearbyTourPlaces(
        mapX: Double,
        mapY: Double,
        radius: Int? ,
        pageNo: Int? ,
        numOfRows: Int? ,
        arrange: String? ,
        type: String? ,
        contentTypeId: String? ,
        mobileOs: String? ,
        mobileApp: String? ,
        areaCode: String? ,
        sigunguCode: String? ,
        cat1: String? ,
        cat2: String? ,
        cat3: String? ,
        lDongRegnCd: String? ,
        lDongSignguCd: String? ,
        lclsSystm1: String? ,
        lclsSystm2: String? ,
        lclsSystm3: String? ,
    ): List<TourPlace> {
        val res = api.getTourListInfo(
            serviceKey   = tourApiKey,
            mapX         = mapX,
            mapY         = mapY,
            radius       = radius ?: 2000,
            pageNo       = pageNo ?: 1,
            numOfRows    = numOfRows ?: 15,
            arrange      = arrange ?: "E",
            type         = (type ?: "json"),               // TourApi는 non-null String
            contentTypeId= contentTypeId ?: "",
            mobileOs     = mobileOs ?: "AND",
            mobileApp    = mobileApp ?: "EVTravel",
            areaCode     = areaCode ?: "",
            sigunguCode  = sigunguCode ?: "",
            cat1         = cat1 ?: "",
            cat2         = cat2 ?: "",
            cat3         = cat3 ?: "",
            lDongRegnCd  = lDongRegnCd ?: "",
            lDongSignguCd= lDongSignguCd ?: "",
            lclsSystm1   = lclsSystm1 ?: "",
            lclsSystm2   = lclsSystm2 ?: "",
            lclsSystm3   = lclsSystm3 ?: "",
        )
        val header = res.response?.header
        if (header?.resultCode != "0000") {
            throw IllegalStateException("Tour API error: ${header?.resultMsg ?: "unknown"} (${header?.resultCode})")
        }
        val dto = res.response?.body?.items?.item.orEmpty()
        return dto.map { it.toDomain() }
    }
}