package com.jeju.evtravel.service.auth

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.DocumentSnapshot
import com.jeju.evtravel.domain.model.PlaceBookmark
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PlaceBookmarkService(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val collection = db.collection("placeBookmarks")

    /** uid + kakao_id 기반 북마크 저장 */
    suspend fun addBookmark(
        uid: String,
        kakaoId: String,
        x: Double,
        y: Double,
        placeName: String,
        description: String,
        imageUrl: String?
    ): Boolean {
        val docId = "${uid}_${kakaoId}"
        val bookmark = PlaceBookmark(
            id = docId,
            uid = uid,
            kakao_id = kakaoId,
            x = x,
            y = y,
            place_name = placeName,
            description = description,
            image_url = imageUrl
        )
        return try {
            collection.document(docId).set(bookmark).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /** uid + kakao_id 기반 단건 조회 */
    suspend fun getBookmark(uid: String, kakaoId: String): PlaceBookmark? {
        val docId = "${uid}_${kakaoId}"
        return try {
            val snapshot = collection.document(docId).get().await()
            if (snapshot.exists()) snapshot.toObject(PlaceBookmark::class.java) else null
        } catch (e: Exception) {
            null
        }
    }

    /** uid 기반 페이지네이션 조회 */
    suspend fun getBookmarksByUid(
        uid: String,
        limit: Long = 10,
        lastSnapshot: DocumentSnapshot? = null
    ): Pair<List<PlaceBookmark>, DocumentSnapshot?> {
        var query = collection
            .whereEqualTo("uid", uid)
            .limit(limit)

        if (lastSnapshot != null) {
            query = query.startAfter(lastSnapshot)
        }

        return try {
            val snapshot = query.get().await()
            val bookmarks = snapshot.toObjects(PlaceBookmark::class.java)
            val lastVisible = snapshot.documents.lastOrNull()
            bookmarks to lastVisible
        } catch (e: Exception) {
            emptyList<PlaceBookmark>() to null
        }
    }

    // 장소 북마크 삭제
    suspend fun deletePlaceBookmarksByUid(uid: String): Boolean {
        val querySnapshot = db.collection("placeBookmarks").whereEqualTo("uid", uid).get().await()
        return try {
            val batch = db.batch()
            for (document in querySnapshot.documents) {
                batch.delete(document.reference)
            }
            batch.commit().await()
            true
        } catch (e: Exception) {
            false
        }
    }

}
