package com.orinasa.app.model

data class LeaveRequest(
    val id: String = "",
    val userId: String = "",
    val companyId: String = "",
    val userFullName: String = "",
    val userPhotoUrl: String = "",
    val departmentName: String = "",
    val type: LeaveType = LeaveType.ANNUAL,
    val startDate: String = "",          // yyyy-MM-dd
    val endDate: String = "",
    val daysCount: Int = 0,
    val reason: String = "",
    val status: LeaveStatus = LeaveStatus.PENDING,
    val reviewedBy: String = "",
    val reviewedByName: String = "",
    val reviewNote: String = "",
    val requestedAt: Long = 0L,
    val reviewedAt: Long = 0L
)

enum class LeaveType(val label: String) {
    ANNUAL("Congé annuel"),
    SICK("Congé maladie"),
    MATERNITY("Congé maternité"),
    PATERNITY("Congé paternité"),
    EXCEPTIONAL("Congé exceptionnel"),
    WITHOUT_PAY("Congé sans solde")
}

enum class LeaveStatus {
    PENDING, APPROVED, REJECTED, CANCELLED
}