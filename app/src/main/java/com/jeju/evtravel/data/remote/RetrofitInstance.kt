package com.jeju.evtravel.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.jeju.evtravel.data.remote.api.KakaoLocalApi

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://dapi.kakao.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val kakaoLocalApi: KakaoLocalApi by lazy {
        retrofit.create(KakaoLocalApi::class.java)
    }
}