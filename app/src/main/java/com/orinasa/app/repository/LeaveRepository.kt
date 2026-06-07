package com.orinasa.app.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.orinasa.app.model.LeaveRequest
import com.orinasa.app.model.LeaveStatus
import com.orinasa.app.model.LeaveType
import com.orinasa.app.utils.DateUtils
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val COL_COMPANIES = "companies"
private const val COL_LEAVES    = "leaves"

class LeaveRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun leavesRef(companyId: String) =
        db.collection(COL_COMPANIES)
            .document(companyId)
            .collection(COL_LEAVES)

    suspend fun submitLeaveRequest(
        companyId: String, userId: String, userFullName: String,
        userPhotoUrl: String, departmentName: String,
        type: LeaveType, startDate: String, endDate: String, reason: String
    ): LeaveRequest {
        val ref       = leavesRef(companyId).document()
        val daysCount = DateUtils.daysBetween(startDate, endDate)
        val request   = LeaveRequest(
            id             = ref.id,
            userId         = userId,
            companyId      = companyId,
            userFullName   = userFullName,
            userPhotoUrl   = userPhotoUrl,
            departmentName = departmentName,
            type           = type,
            startDate      = startDate,
            endDate        = endDate,
            daysCount      = daysCount,
            reason         = reason,
            status         = LeaveStatus.PENDING,
            requestedAt    = System.currentTimeMillis()
        )
        ref.set(request).await()
        return request
    }

    suspend fun approveLeave(
        companyId: String, leaveId: String,
        reviewedById: String, reviewedByName: String, note: String = ""
    ) {
        leavesRef(companyId).document(leaveId).update(
            "status",         LeaveStatus.APPROVED.name,
            "reviewedBy",     reviewedById,
            "reviewedByName", reviewedByName,
            "reviewNote",     note,
            "reviewedAt",     System.currentTimeMillis()
        ).await()
    }

    suspend fun rejectLeave(
        companyId: String, leaveId: String,
        reviewedById: String, reviewedByName: String, note: String
    ) {
        leavesRef(companyId).document(leaveId).update(
            "status",         LeaveStatus.REJECTED.name,
            "reviewedBy",     reviewedById,
            "reviewedByName", reviewedByName,
            "reviewNote",     note,
            "reviewedAt",     System.currentTimeMillis()
        ).await()
    }

    suspend fun cancelLeave(companyId: String, leaveId: String) {
        leavesRef(companyId).document(leaveId)
            .update("status", LeaveStatus.CANCELLED.name).await()
    }

    fun listenToUserLeaves(companyId: String, userId: String): Flow<List<LeaveRequest>> =
        callbackFlow {
            val l = leavesRef(companyId)
                .whereEqualTo("userId", userId)
                .orderBy("requestedAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snap, _ ->
                    trySend(snap?.documents?.mapNotNull {
                        it.toObject(LeaveRequest::class.java)
                    } ?: emptyList())
                }
            awaitClose { l.remove() }
        }

    fun listenToPendingLeaves(companyId: String): Flow<List<LeaveRequest>> = callbackFlow {
        val l = leavesRef(companyId)
            .whereEqualTo("status", LeaveStatus.PENDING.name)
            .orderBy("requestedAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.mapNotNull {
                    it.toObject(LeaveRequest::class.java)
                } ?: emptyList())
            }
        awaitClose { l.remove() }
    }

    fun listenToAllLeaves(companyId: String): Flow<List<LeaveRequest>> = callbackFlow {
        val l = leavesRef(companyId)
            .orderBy("requestedAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.mapNotNull {
                    it.toObject(LeaveRequest::class.java)
                } ?: emptyList())
            }
        awaitClose { l.remove() }
    }

    suspend fun getActiveLeavesBetween(
        companyId: String, userId: String,
        startDate: String, endDate: String
    ): List<LeaveRequest> = runCatching {
        leavesRef(companyId)
            .whereEqualTo("userId", userId)
            .whereIn("status", listOf(LeaveStatus.PENDING.name, LeaveStatus.APPROVED.name))
            .get().await()
            .documents.mapNotNull { it.toObject(LeaveRequest::class.java) }
            .filter { it.startDate <= endDate && it.endDate >= startDate }
    }.getOrElse { emptyList() }
}