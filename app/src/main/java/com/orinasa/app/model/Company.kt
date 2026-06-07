package com.orinasa.app.model

data class Company(
    val id: String = "",
    val name: String = "",
    val sector: String = "",             // BTP, Commerce, ONG, etc.
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val logoUrl: String = "",
    val nif: String = "",                // Numéro fiscal malgache
    val stat: String = "",               // Numéro STAT
    val cnapsNumber: String = "",
    val inviteCode: String = "",         // Code 6 chiffres pour inviter des employés
    val adminId: String = "",
    val employeeCount: Int = 0,
    val createdAt: Long = 0L
)