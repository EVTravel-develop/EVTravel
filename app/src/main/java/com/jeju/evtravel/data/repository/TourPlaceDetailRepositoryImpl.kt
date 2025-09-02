package com.jeju.evtravel.data.repository

import com.jeju.evtravel.data.remote.api.TourPlaceApi
import com.jeju.evtravel.data.remote.mapper.toDomain
import com.jeju.evtravel.domain.model.TourPlaceDetail
import com.jeju.evtravel.domain.repository.TourPlaceDetailRepository

class TourPlaceDetailRepositoryImpl (
    private val api: TourPlaceApi,
    private val tourApiKey: String
) : TourPlaceDetailRepository {
    override suspend fun getTourPlaceContentId(
        contentId: String,
        pageNo: Int?,
        numOfRows: Int?,
        os: String,
        app: String,
        type: String
    ): TourPlaceDetail {
        val dto = api.getTourPlaceDetail(
            serviceKey = tourApiKey,
            pageNo = pageNo ?: 1,
            numOfRows = numOfRows ?: 15,
            os = os,
            app = app,
            type = type,
            contentId = contentId
        )
        val item = dto?.response?.body?.items?.item?.firstOrNull()
            ?: throw IllegalStateException("상세 정보가 없습니다. contentId=$contentId")

        return item.toDomain()
    }
}