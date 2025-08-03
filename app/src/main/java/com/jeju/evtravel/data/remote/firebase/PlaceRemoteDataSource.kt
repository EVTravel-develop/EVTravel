package com.jeju.evtravel.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.jeju.evtravel.data.model.PlaceDto
import kotlinx.coroutines.tasks.await

/**
 * Firebase Firestore에서 장소 데이터를 가져오는 데이터 소스 클래스
 */
class PlaceRemoteDataSource {

    // Firebase Firestore 인스턴스
    private val db = FirebaseFirestore.getInstance()

    /**
     * Firestore에서 모든 장소 데이터를 가져옵니다.
     * @return 장소 DTO 리스트 (에러 발생 시 빈 리스트 반환)
     */
    suspend fun fetchPlaces(): List<PlaceDto> {
        return try {
            // 'places' 컬렉션에서 모든 문서를 가져와 PlaceDto로 변환
            val snapshot = db.collection("places").get().await()
            snapshot.documents.mapNotNull { it.toObject(PlaceDto::class.java) }
        } catch (e: Exception) {
            // 예외 발생 시 빈 리스트 반환
            emptyList()
        }
    }
}