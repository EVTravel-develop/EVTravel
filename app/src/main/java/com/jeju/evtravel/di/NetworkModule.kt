package com.jeju.evtravel.di

import com.jeju.evtravel.BuildConfig
import com.jeju.evtravel.data.remote.api.ChargerApi
import com.jeju.evtravel.data.remote.api.KakaoLocalApi
import com.jeju.evtravel.data.remote.api.KakaoLocalRegionApi
import com.jeju.evtravel.data.repository.ChargerRepository as ChargerListRepository
import com.jeju.evtravel.data.repository.PlaceRepositoryImpl
import com.jeju.evtravel.data.repository.RegionCodeRepository
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
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // kakao 공통 Retrofit
    private const val BASE_URL = "https://dapi.kakao.com"

    // 공통 OkHttpClient
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    // Kakao Local Api 키
    @Provides
    @Singleton
    @Named("KAKAO_REST_API_KEY")
    fun provideKakaoRestApiKey(): String = BuildConfig.KAKAO_REST_API_KEY

    // 공공데이터 전기차 충전소 Api 키
    @Provides
    @Singleton
    @Named("EV_CHARGER_API_KEY")
    fun provideEvChargerApiKey(): String = BuildConfig.EV_CHARGER_API_KEY

    // 공공데이터 전기차 충전소 Retrofit
    @Provides
    @Singleton
    fun provideChargerApi(
        client: OkHttpClient
    ): ChargerApi {
        return Retrofit.Builder()
            .baseUrl("https://apis.data.go.kr") // 공공데이터 API base URL
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ChargerApi::class.java)
    }

    // 키워드로 장소 검색하기
    @Provides
    @Singleton
    fun provideKakaoLocalApi(retrofit: Retrofit): KakaoLocalApi =
        retrofit.create(KakaoLocalApi::class.java)

    // 좌표로 행정구역정보 변환
    @Provides
    @Singleton
    fun provideKakaoLocalRegionApi(retrofit: Retrofit): KakaoLocalRegionApi =
        retrofit.create(KakaoLocalRegionApi::class.java)



    // 키워드로 장소 검색 Repository
    @Provides
    @Singleton
    fun providePlaceRepository(
        api: KakaoLocalApi,
        @Named("KAKAO_REST_API_KEY") key: String
    ): PlaceRepository = PlaceRepositoryImpl(api, key)

    // 키워드로 장소 검색 UseCase
    @Provides
    @Singleton
    fun provideSearchNearbyPlacesUseCase(repo: PlaceRepository) =
        SearchNearbyPlacesUseCase(repo)

    // 좌표로 행정구역정보 변환 Repository
    @Provides
    @Singleton
    fun provideRegionCodeRepository(
        api: KakaoLocalRegionApi,
        @Named("KAKAO_REST_API_KEY") key: String
    ): RegionCodeRepository = RegionCodeRepository(api, key)

    // 공공데이터 전기차 충전소 Repository
    @Provides
    @Singleton
    fun provideChargerRepository(
        api: ChargerApi,
        @Named("EV_CHARGER_API_KEY") key: String
    ): ChargerListRepository {
        return ChargerListRepository(api, key)
    }
}
