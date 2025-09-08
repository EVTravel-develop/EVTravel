// CourseBookmarkService.kt
package com.jeju.evtravel.service.auth

import android.util.Log // Log 임포트
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.jeju.evtravel.domain.model.CourseBookmark
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CourseBookmarkService(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val collection = db.collection("courseBookmarks")

    suspend fun addBookmark(
        uid: String,
        courseId: String,
        courseName: String,
        courseDescription: String,
        imageUrl: String
    ): Boolean {
        val docId = "${uid}_${courseId}"
        val bookmark = CourseBookmark(
            id = docId,
            uid = uid,
            course_id = courseId,
            course_name = courseName,
            course_description = courseDescription,
            imageUrl = imageUrl,
        )
        return try {
            collection.document(docId).set(bookmark).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getBookmark(uid: String, courseId: String): CourseBookmark? {
        val docId = "${uid}_${courseId}"
        return try {
            val snapshot = collection.document(docId).get().await()
            if (snapshot.exists()) snapshot.toObject(CourseBookmark::class.java) else null
        } catch (e: Exception) {
            null
        }
    }

    // 코스 북마크 삭제
    suspend fun deleteCourseBookmarksByUid(uid: String): Boolean {
        val querySnapshot = db.collection("courseBookmarks").whereEqualTo("uid", uid).get().await()
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

    suspend fun getBookmarksByUid(
        uid: String,
        limit: Long = 10,
        lastSnapshot: DocumentSnapshot? = null
    ): Pair<List<CourseBookmark>, DocumentSnapshot?> {
        Log.d("BookmarkService", "Fetching bookmarks for UID: $uid")
        var query = collection
            .whereEqualTo("uid", uid)
            .limit(limit)

        if (lastSnapshot != null) {
            query = query.startAfter(lastSnapshot)
        }

        return try {
            val snapshot = query.get().await()
            val bookmarks = snapshot.toObjects(CourseBookmark::class.java)
            Log.d("BookmarkService", "Raw snapshot result: $snapshot")

            // ✅ 로그 추가: Firestore에서 가져온 데이터 확인
            Log.d("BookmarkService", "Fetched ${bookmarks.size} bookmarks for UID: $uid")
            bookmarks.forEach { bookmark ->
                Log.d("BookmarkService", "Bookmark data: name=${bookmark.course_name}, desc=${bookmark.course_description}, image=${bookmark.imageUrl}")
            }

            val lastVisible = snapshot.documents.lastOrNull()
            bookmarks to lastVisible
        } catch (e: Exception) {
            Log.e("BookmarkService", "Error fetching bookmarks", e)
            emptyList<CourseBookmark>() to null
        }
    }
}