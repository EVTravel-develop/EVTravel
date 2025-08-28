package com.jeju.evtravel.di

import com.jeju.evtravel.BuildConfig
import com.jeju.evtravel.data.remote.api.ChargerApi
import com.jeju.evtravel.data.remote.api.KakaoLocalApi
import com.jeju.evtravel.data.remote.api.KakaoLocalRegionApi
import com.jeju.evtravel.data.remote.api.TourApi
import com.jeju.evtravel.data.repository.ChargerRepository as ChargerListRepository
import com.jeju.evtravel.data.repository.PlaceRepositoryImpl
import com.jeju.evtravel.data.repository.RegionCodeRepository
import com.jeju.evtravel.data.repository.TourPlaceRepositoryImpl
import com.jeju.evtravel.domain.repository.PlaceRepository
import com.jeju.evtravel.domain.repository.TourPlaceRepository
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
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // kakao 공통 Retrofit
    private const val KAKAO_BASE = "https://dapi.kakao.com/"
    private const val DATA_BASE = "https://apis.data.go.kr/"

    // 공통 OkHttpClient
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides @Singleton @Named("KAKAO_RETROFIT")
    fun provideKakaoRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(KAKAO_BASE)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

    @Provides @Singleton @Named("DATA_RETROFIT")
    fun provideDataRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(DATA_BASE)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

    // Kakao Local
    @Provides @Singleton
    fun provideKakaoLocalApi(@Named("KAKAO_RETROFIT") retrofit: Retrofit): KakaoLocalApi =
        retrofit.create(KakaoLocalApi::class.java)

    @Provides @Singleton
    fun provideKakaoLocalRegionApi(@Named("KAKAO_RETROFIT") retrofit: Retrofit): KakaoLocalRegionApi =
        retrofit.create(KakaoLocalRegionApi::class.java)

    // 공공데이터 - 충전소
    @Provides @Singleton
    fun provideChargerApi(@Named("DATA_RETROFIT") retrofit: Retrofit): ChargerApi =
        retrofit.create(ChargerApi::class.java)

    // 공공데이터 - Tour
    @Provides @Singleton
    fun provideTourApi(@Named("DATA_RETROFIT") retrofit: Retrofit): TourApi =
        retrofit.create(TourApi::class.java)

    // API Keys
    @Provides @Singleton @Named("KAKAO_REST_API_KEY")
    fun provideKakaoRestApiKey(): String = BuildConfig.KAKAO_REST_API_KEY

    @Provides @Singleton @Named("EV_CHARGER_API_KEY")
    fun provideEvChargerApiKey(): String = BuildConfig.EV_CHARGER_API_KEY

    @Provides @Singleton @Named("Tour_API_KEY")
    fun provideTourApiKey(): String = BuildConfig.EV_CHARGER_API_KEY

    // Place Repository / UseCase
    @Provides @Singleton
    fun providePlaceRepository(
        api: KakaoLocalApi,
        @Named("KAKAO_REST_API_KEY") key: String
    ): PlaceRepository = PlaceRepositoryImpl(api, key)

    @Provides @Singleton
    fun provideSearchNearbyPlacesUseCase(repo: PlaceRepository) =
        SearchNearbyPlacesUseCase(repo)

    @Provides @Singleton
    fun provideRegionCodeRepository(
        api: KakaoLocalRegionApi,
        @Named("KAKAO_REST_API_KEY") key: String
    ): RegionCodeRepository = RegionCodeRepository(api, key)

    // Charger Repository
    @Provides @Singleton
    fun provideChargerRepository(
        api: ChargerApi,
        @Named("EV_CHARGER_API_KEY") key: String
    ): ChargerListRepository = ChargerListRepository(api, key)

    // Tour Repository
    @Provides @Singleton
    fun provideTourRepository(
        api: TourApi,
        @Named("Tour_API_KEY") key: String
    ): TourPlaceRepository = TourPlaceRepositoryImpl(api, key)
}