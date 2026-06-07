package com.orinasa.app.model

data class Attendance(
    val id: String = "",
    val userId: String = "",
    val companyId: String = "",
    val userFullName: String = "",
    val userPhotoUrl: String = "",
    val departmentName: String = "",
    val date: String = "",               // yyyy-MM-dd
    val clockInTime: String = "",        // HH:mm
    val clockOutTime: String = "",
    val clockInTimestamp: Long = 0L,
    val clockOutTimestamp: Long = 0L,
    val status: AttendanceStatus = AttendanceStatus.PRESENT,
    val isLate: Boolean = false,
    val lateMinutes: Int = 0,
    val workDurationMinutes: Int = 0,
    val clockInMethod: String = "button", // "button" | "qr" | "gps"
    val note: String = ""
) {
    fun isComplete() = clockInTime.isNotBlank() && clockOutTime.isNotBlank()
}

enum class AttendanceStatus {
    PRESENT, ABSENT, LATE, HALF_DAY, ON_LEAVE, HOLIDAY
}