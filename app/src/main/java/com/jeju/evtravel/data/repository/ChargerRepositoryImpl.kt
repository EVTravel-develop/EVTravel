package com.jeju.evtravel.data.repository

import com.jeju.evtravel.data.remote.api.KakaoLocalApi
import com.jeju.evtravel.domain.model.Charger
import com.jeju.evtravel.domain.repository.ChargerRepository
import android.util.Log
import com.google.gson.Gson
import com.jeju.evtravel.data.remote.dto.KakaoErrorDto
import timber.log.Timber
import java.io.IOException

class ChargerRepositoryImpl(
    private val api: KakaoLocalApi,
    private val restApiKey: String
) : ChargerRepository {

    private val gson = Gson()

    override suspend fun getNearbyChargers(lat: Double, lon: Double): List<Charger> {
        val response = api.searchChargers(
            authorization = "KakaoAK $restApiKey",
            x = lon,
            y = lat
        )

        if (!response.isSuccessful) {
            val code = response.code()
            val raw = response.errorBody()?.string().orEmpty()
            val err = runCatching { gson.fromJson(raw, KakaoErrorDto::class.java) }.getOrNull()
            Timber.tag("KAKAO_LOCAL").e("HTTP $code ${raw.take(300)}")
            val msg = buildString {
                append("Kakao Local API error $code")
                if (!err?.errorType.isNullOrBlank()) append(" [${err?.errorType}]")
                if (!err?.message.isNullOrBlank()) append(": ${err?.message}")
            }
            throw IOException(msg.ifBlank { "Kakao Local API error $code" })
        }

        val body = response.body() ?: throw IOException("Empty body from Kakao Local (HTTP ${response.code()})")
        return body.documents.map {
            Charger(
                name = it.placeName,
                address = it.roadAddressName ?: it.addressName ?: "주소 없음",
                latitude = it.y.toDoubleOrNull() ?: 0.0,
                longitude = it.x.toDoubleOrNull() ?: 0.0
            )
        }
    }
}