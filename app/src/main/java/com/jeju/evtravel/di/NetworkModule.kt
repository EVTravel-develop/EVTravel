package com.jeju.evtravel.di

import com.jeju.evtravel.BuildConfig
import com.jeju.evtravel.data.remote.api.KakaoLocalApi
import com.jeju.evtravel.data.repository.PlaceRepositoryImpl
import com.jeju.evtravel.domain.repository.PlaceRepository
import com.jeju.evtravel.domain.usecase.SearchNearbyPlacesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://dapi.kakao.com"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    @Provides
    @Singleton
    fun provideKakaoLocalApi(retrofit: Retrofit): KakaoLocalApi =
        retrofit.create(KakaoLocalApi::class.java)

    @Provides
    @Singleton
    @Named("KAKAO_REST_API_KEY")
    fun provideKakaoRestApiKey(): String = BuildConfig.KAKAO_REST_API_KEY // gradle에 넣어두세요

    @Provides
    @Singleton
    fun providePlaceRepository(
        api: KakaoLocalApi,
        @Named("KAKAO_REST_API_KEY") key: String
    ): PlaceRepository = PlaceRepositoryImpl(api, key)

    @Provides
    @Singleton
    fun provideSearchNearbyPlacesUseCase(repo: PlaceRepository) =
        SearchNearbyPlacesUseCase(repo)
}
