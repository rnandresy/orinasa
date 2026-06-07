package com.orinasa.app.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.orinasa.app.model.Announcement
import com.orinasa.app.model.AnnouncementPriority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val COL_COMPANIES     = "companies"
private const val COL_ANNOUNCEMENTS = "announcements"

class AnnouncementRepository(private val companyId: String) {

    private val db = FirebaseFirestore.getInstance()

    private val ref = db.collection(COL_COMPANIES)
        .document(companyId)
        .collection(COL_ANNOUNCEMENTS)

    fun listenToAnnouncements(deptId: String = ""): Flow<List<Announcement>> = callbackFlow {
        val l = ref
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull {
                    it.toObject(Announcement::class.java)
                } ?: emptyList()
                trySend(
                    if (deptId.isBlank()) list
                    else list.filter {
                        it.targetDepartmentId.isBlank() || it.targetDepartmentId == deptId
                    }
                )
            }
        awaitClose { l.remove() }
    }

    suspend fun publishAnnouncement(
        title: String,
        content: String,
        authorId: String,
        authorName: String,
        priority: AnnouncementPriority = AnnouncementPriority.NORMAL,
        targetDeptId: String = "",
        targetDeptName: String = ""
    ): String {
        val docRef = ref.document()
        docRef.set(
            Announcement(
                id                   = docRef.id,
                companyId            = companyId,
                title                = title.trim(),
                content              = content.trim(),
                authorId             = authorId,
                authorName           = authorName,
                priority             = priority,
                targetDepartmentId   = targetDeptId,
                targetDepartmentName = targetDeptName,
                timestamp            = System.currentTimeMillis()
            )
        ).await()
        return docRef.id
    }

    suspend fun markAsRead(announcementId: String, userId: String) {
        ref.document(announcementId)
            .update("readBy", FieldValue.arrayUnion(userId))
            .await()
    }

    suspend fun deleteAnnouncement(announcementId: String) {
        ref.document(announcementId).delete().await()
    }
}