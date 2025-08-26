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
    zscode: String
    ): List<ChargerInfo> {

        val result = mutableListOf<ChargerInfo>()
        val rowsPerPage = 100
        var totalPages = 1

        try {
            // 첫 페이지 row 응답 확인용 로그
            val firstRaw = api.getChargerInfoRaw(
                serviceKey = apiKey,
                zcode = zcode,
                zscode = zscode,
                pageNo = 1,
                numOfRows = rowsPerPage
            )

            if (BuildConfig.DEBUG) {
                val body: ResponseBody? = firstRaw.body()
                val raw = body?.string().orEmpty()
                Log.d("ChargerFetchRaw", "page=1 preview=${raw.take(1000)}")
            }

            // 첫 페이지 호출 → totalCount 확인
            val first = api.getChargerInfo(
                serviceKey = apiKey,
                zcode = zcode,
                zscode = zscode,
                pageNo = 1,
                numOfRows = rowsPerPage
            )
            val totalCount = first.totalCount
            totalPages = (totalCount + rowsPerPage - 1) / rowsPerPage

            Log.d("ChargerFetch", "전체 항목 수: $totalCount → 총 페이지 수: $totalPages")

            val firstItems = first.items.item
            result.addAll(firstItems.map { it.toDomain() })

            // 2페이지부터 반복
            for (page in 2..totalPages) {
//                delay(500L)
                val dto = api.getChargerInfo(
                    serviceKey = apiKey,
                    zcode = zcode,
                    zscode = zscode,
                    pageNo = page,
                    numOfRows = rowsPerPage
                )
                val items = dto.items.item
                Log.d("ChargerFetch", "page=$page → ${items.size}개 항목 수신")
                result.addAll(items.map { it.toDomain() })
            }

        } catch (e: Exception) {
            Log.e("ChargerFetch", "API 호출 중 오류 발생", e)
        }

        return result
    }
}