package com.jeju.evtravel.service.auth

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jeju.evtravel.domain.model.CourseBookmark
import kotlinx.coroutines.tasks.await

class CourseBookmarkService(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val collection = db.collection("course_bookmarks")

    /** uid + course_id 로 북마크 저장 */
    suspend fun addBookmark(uid: String, courseId: String): Boolean {
        val docId = "${uid}_${courseId}"
        val bookmark = CourseBookmark(
            id = docId,
            uid = uid,
            course_id = courseId,
            createdAt = Timestamp.now()
        )
        return try {
            collection.document(docId).set(bookmark).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /** uid + course_id 로 단건 조회 */
    suspend fun getBookmark(uid: String, courseId: String): CourseBookmark? {
        val docId = "${uid}_${courseId}"
        return try {
            val snapshot = collection.document(docId).get().await()
            if (snapshot.exists()) snapshot.toObject(CourseBookmark::class.java) else null
        } catch (e: Exception) {
            null
        }
    }

    /** uid + course_id 로 북마크 삭제 */
    suspend fun removeBookmark(uid: String, courseId: String): Boolean {
        val docId = "${uid}_${courseId}"
        return try {
            collection.document(docId).delete().await()
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
        var query = collection
            .whereEqualTo("uid", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)

        if (lastSnapshot != null) {
            query = query.startAfter(lastSnapshot)
        }

        val snapshot = query.get().await()
        val bookmarks = snapshot.toObjects(CourseBookmark::class.java)
        val lastVisible = snapshot.documents.lastOrNull()
        return bookmarks to lastVisible
    }
}