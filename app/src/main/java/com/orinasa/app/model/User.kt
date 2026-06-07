package com.orinasa.app.model

import com.google.firebase.firestore.PropertyName

data class User(
    val userId: String = "",
    val companyId: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val departmentId: String = "",
    val departmentName: String = "",
    val position: String = "",           // Poste / Titre
    val baseSalary: Double = 0.0,        // Salaire de base en Ariary
    val leaveBalance: Int = 30,          // Solde congés annuel (jours)
    val leaveUsed: Int = 0,
    val startDate: String = "",          // yyyy-MM-dd
    val contractType: String = "CDI",    // CDI | CDD | Stage | Consultant
    val workSchedule: WorkSchedule = WorkSchedule(),
    @get:PropertyName("isAdmin")
    @set:PropertyName("isAdmin")
    var isAdmin: Boolean = false,
    @get:PropertyName("isActive")
    @set:PropertyName("isActive")
    var isActive: Boolean = true,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    fun fullName() = "$firstName $lastName".trim()
    fun leaveRemaining() = (leaveBalance - leaveUsed).coerceAtLeast(0)
    fun initials(): String {
        val f = firstName.firstOrNull()?.uppercase() ?: ""
        val l = lastName.firstOrNull()?.uppercase() ?: ""
        return "$f$l"
    }
}

data class WorkSchedule(
    val startTime: String = "08:00",     // HH:mm
    val endTime: String = "17:00",
    val workDays: List<Int> = listOf(1, 2, 3, 4, 5), // 1=Lun ... 7=Dim
    val lateToleranceMinutes: Int = 15
)