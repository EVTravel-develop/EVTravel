package com.jeju.evtravel.domain.usecase

import com.jeju.evtravel.domain.model.TourPlaceDetail
import com.jeju.evtravel.domain.repository.TourPlaceDetailRepository
import javax.inject.Inject

class TourPlaceDetailUseCase @Inject constructor(
    private val repository: TourPlaceDetailRepository
) {
    suspend operator fun invoke(contentId: String): TourPlaceDetail {
        return repository.getTourPlaceContentId(contentId = contentId)
    }
}