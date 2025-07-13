package com.jeju.evtravel.data.repository

import com.jeju.evtravel.data.remote.firebase.PlaceRemoteDataSource
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.domain.repository.PlaceRepository

/**
 * PlaceRepository 인터페이스의 구현체 클래스
 * 원격 데이터 소스에서 데이터를 가져와 도메인 모델로 변환
 *
 * @property remoteDataSource 원격 데이터 소스 (기본값: PlaceRemoteDataSource())
 */
class PlaceRepositoryImpl(
    private val remoteDataSource: PlaceRemoteDataSource = PlaceRemoteDataSource()
) : PlaceRepository {

    /**
     * 모든 장소 정보를 비동기적으로 가져옵니다.
     * 원격 데이터 소스에서 DTO를 가져와 도메인 모델로 변환합니다.
     *
     * @return 장소 도메인 모델 리스트
     */
    override suspend fun getPlaces(): List<Place> {
        // 원격 데이터 소스에서 DTO 리스트를 가져와 도메인 모델로 변환
        return remoteDataSource.fetchPlaces().map { dto ->
            Place(
                id = dto.id,
                name = dto.name,
                address = dto.address,
                type = dto.type
            )
        }
    }
}
