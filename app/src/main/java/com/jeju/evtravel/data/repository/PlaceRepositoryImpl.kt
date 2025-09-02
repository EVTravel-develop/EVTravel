package com.jeju.evtravel.data.repository

import com.google.gson.Gson
import com.jeju.evtravel.data.remote.api.KakaoLocalApi
import com.jeju.evtravel.data.remote.dto.KakaoErrorDto
import com.jeju.evtravel.data.remote.mapper.toDomain
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.domain.repository.PlaceRepository
import timber.log.Timber
import java.io.IOException

/**
 * PlaceRepositoryImpl은 PlaceRepository 인터페이스를 구현하여
 * 카카오 로컬 API를 통해 장소 검색 기능을 제공합니다.
 *
 * @property api 카카오 로컬 API 인스턴스
 * @property restApiKey 카카오 REST API 키
 */
class PlaceRepositoryImpl(
    private val api: KakaoLocalApi,
    private val restApiKey: String
) : PlaceRepository {
    /**
     * 지정된 좌표 주변의 장소를 검색합니다.
     */
    private val gson = Gson()

    override suspend fun searchNearbyPlaces(
        query: String,
        x: Double?,
        y: Double?,
        radius: Int?,
        page: Int?,
        size: Int?
    ): List<Place> {
        val res = api.searchKeyword(
            authorization = "KakaoAK $restApiKey",
            query = query,
            x = x,
            y = y,
            radius = radius,
            page = page,
            size = size
        )
        if (!res.isSuccessful) {
            val code = res.code()
            val raw = res.errorBody()?.string().orEmpty()
            val err = runCatching { gson.fromJson(raw, KakaoErrorDto::class.java) }.getOrNull()

            Timber.tag("KAKAO_LOCAL").e("HTTP $code ${raw.take(300)}")
            val msg = buildString {
                append("Kakao Local API error $code")
                if (!err?.errorType.isNullOrBlank()) append(" [${err?.errorType}]")
                if (!err?.message.isNullOrBlank()) append(": ${err?.message}")
            }
            throw IOException(msg.ifBlank { "Kakao Local API error $code" })
        }

        val dto = res.body() ?: throw IOException("Kakao Local API empty body (HTTP ${res.code()})")
        return dto.documents.map { it.toDomain() }
    }
}