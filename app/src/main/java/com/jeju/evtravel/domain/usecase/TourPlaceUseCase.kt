package com.jeju.evtravel.domain.usecase

import com.jeju.evtravel.domain.model.TourPlace
import com.jeju.evtravel.domain.repository.TourPlaceRepository
import javax.inject.Inject

class TourPlaceUseCase @Inject constructor(
    private val repository: TourPlaceRepository
) {
    suspend operator fun invoke(
        x: Double,
        y: Double,
        radius: Int? = 2000, // 기본 2km 반경
        arrange: String? = "E",
        contentTypeId: String? = null,
        areaCode: String? = "",
        sigunguCode: String? = "",
        cat1: String? = "",
        cat2: String? = "",
        cat3: String? = "",
        lDongRegnCd: String? = "",
        lDongSignguCd: String? = "",
        lclsSystm1: String? = "",
        lclsSystm2: String? = "",
        lclsSystm3: String? = "",
    ): List<TourPlace> {
        return repository.getNearbyTourPlaces(
            x,
            y,
            radius,
            arrange = arrange,
            contentTypeId = contentTypeId,
            areaCode = areaCode,
            sigunguCode = sigunguCode,
            cat1 = cat1,
            cat2 = cat2,
            cat3 = cat3,
            lDongRegnCd = lDongRegnCd,
            lDongSignguCd = lDongSignguCd,
            lclsSystm1 = lclsSystm1,
            lclsSystm2 = lclsSystm2,
            lclsSystm3 = lclsSystm3,
        )
    }
}