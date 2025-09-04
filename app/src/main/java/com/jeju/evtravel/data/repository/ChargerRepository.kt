package com.jeju.evtravel.data.repository

import android.util.Log
import com.jeju.evtravel.data.remote.api.ChargerApi
import com.jeju.evtravel.data.remote.mapper.toDomain
import com.jeju.evtravel.domain.model.ChargerInfo
import javax.inject.Inject
import com.jeju.evtravel.BuildConfig
import okhttp3.ResponseBody

class ChargerRepository @Inject constructor(
    private val api: ChargerApi,
    private val apiKey: String
) {
    suspend fun fetchChargers(
    zcode: String,
    zscode: String,
    statId: String
    ): List<ChargerInfo> {

        val result = mutableListOf<ChargerInfo>()
        val size = 100
        var page = 1

        try {
            // 첫 페이지 row 응답 확인용 로그
            val firstRaw = api.getChargerInfoRaw(
                serviceKey = apiKey,
                zcode = zcode,
                zscode = zscode,
                statId = statId,
                pageNo = 1,
                numOfRows = size
            )

            if (BuildConfig.DEBUG) {
                val body: ResponseBody? = firstRaw.body()
                val raw = body?.string().orEmpty()
                Log.d("ChargerFetchRaw", "page=1 preview=${raw.take(1000)}")
            }

            val dto = api.getChargerInfo(
                serviceKey = apiKey,
                zcode = zcode,
                zscode = zscode,
                statId = statId,
                pageNo = page,
                numOfRows = size
            )
            val items = dto.items.item
            Log.d("ChargerFetch", "page=$page → ${items.size}개 항목 수신")
            result.addAll(items.map { it.toDomain() })

        } catch (e: Exception) {
            Log.e("ChargerFetch", "API 호출 중 오류 발생", e)
        }

        return result
    }
}