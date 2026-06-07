package com.orinasa.app.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.orinasa.app.model.Attendance
import com.orinasa.app.model.AttendanceStatus
import com.orinasa.app.utils.DateUtils
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val COL_COMPANIES  = "companies"
private const val COL_ATTENDANCE = "attendance"

class AttendanceRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun attendanceRef(companyId: String) =
        db.collection(COL_COMPANIES)
            .document(companyId)
            .collection(COL_ATTENDANCE)

    suspend fun clockIn(
        companyId: String,
        userId: String,
        userFullName: String,
        userPhotoUrl: String,
        departmentName: String,
        scheduledStart: String,
        toleranceMinutes: Int,
        method: String = "button"
    ): Attendance {
        val now     = DateUtils.now()
        val today   = DateUtils.today()
        val isLate  = DateUtils.isLate(now, scheduledStart, toleranceMinutes)
        val lateMin = if (isLate) DateUtils.lateMinutes(now, scheduledStart) else 0
        val ref     = attendanceRef(companyId).document("${userId}_$today")

        val attendance = Attendance(
            id               = ref.id,
            userId           = userId,
            companyId        = companyId,
            userFullName     = userFullName,
            userPhotoUrl     = userPhotoUrl,
            departmentName   = departmentName,
            date             = today,
            clockInTime      = now,
            clockInTimestamp = System.currentTimeMillis(),
            status           = if (isLate) AttendanceStatus.LATE else AttendanceStatus.PRESENT,
            isLate           = isLate,
            lateMinutes      = lateMin,
            clockInMethod    = method
        )
        ref.set(attendance).await()
        return attendance
    }

    suspend fun clockOut(companyId: String, userId: String): Attendance? {
        val today    = DateUtils.today()
        val ref      = attendanceRef(companyId).document("${userId}_$today")
        val record   = ref.get().await().toObject(Attendance::class.java) ?: return null
        val now      = DateUtils.now()
        val duration = DateUtils.minutesBetween(record.clockInTime, now)
        ref.update(
            "clockOutTime",        now,
            "clockOutTimestamp",   System.currentTimeMillis(),
            "workDurationMinutes", duration
        ).await()
        return record.copy(clockOutTime = now, workDurationMinutes = duration)
    }

    suspend fun getTodayAttendance(companyId: String, userId: String): Attendance? =
        runCatching {
            attendanceRef(companyId)
                .document("${userId}_${DateUtils.today()}")
                .get().await()
                .toObject(Attendance::class.java)
        }.getOrNull()

    fun listenTodayAttendance(companyId: String, userId: String): Flow<Attendance?> =
        callbackFlow {
            val l = attendanceRef(companyId)
                .document("${userId}_${DateUtils.today()}")
                .addSnapshotListener { snap, _ ->
                    trySend(snap?.toObject(Attendance::class.java))
                }
            awaitClose { l.remove() }
        }

    fun listenToUserAttendance(
        companyId: String, userId: String, limit: Long = 30
    ): Flow<List<Attendance>> = callbackFlow {
        val l = attendanceRef(companyId)
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.mapNotNull {
                    it.toObject(Attendance::class.java)
                } ?: emptyList())
            }
        awaitClose { l.remove() }
    }

    fun listenTeamAttendanceToday(companyId: String): Flow<List<Attendance>> = callbackFlow {
        val l = attendanceRef(companyId)
            .whereEqualTo("date", DateUtils.today())
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.mapNotNull {
                    it.toObject(Attendance::class.java)
                } ?: emptyList())
            }
        awaitClose { l.remove() }
    }

    suspend fun getUserMonthlyStats(
        companyId: String, userId: String, month: Int, year: Int
    ): Map<String, Int> {
        val prefix  = "%04d-%02d".format(year, month)
        val records = runCatching {
            attendanceRef(companyId)
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("date", "${prefix}-01")
                .whereLessThanOrEqualTo("date",    "${prefix}-31")
                .get().await()
                .documents.mapNotNull { it.toObject(Attendance::class.java) }
        }.getOrElse { emptyList() }

        return mapOf(
            "present"      to records.count {
                it.status == AttendanceStatus.PRESENT || it.status == AttendanceStatus.LATE
            },
            "absent"       to records.count { it.status == AttendanceStatus.ABSENT },
            "late"         to records.count { it.isLate },
            "onLeave"      to records.count { it.status == AttendanceStatus.ON_LEAVE },
            "totalMinutes" to records.sumOf { it.workDurationMinutes }
        )
    }

    suspend fun markAbsent(
        companyId: String, userId: String, userFullName: String,
        date: String = DateUtils.today(), note: String = ""
    ) {
        val ref = attendanceRef(companyId).document("${userId}_$date")
        ref.set(
            Attendance(
                id           = ref.id,
                userId       = userId,
                companyId    = companyId,
                userFullName = userFullName,
                date         = date,
                status       = AttendanceStatus.ABSENT,
                note         = note
            )
        ).await()
    }
}