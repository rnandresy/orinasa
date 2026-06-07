package com.orinasa.app.model

data class Payslip(
    val id: String = "",
    val userId: String = "",
    val companyId: String = "",
    val userFullName: String = "",
    val position: String = "",
    val month: Int = 0,                  // 1-12
    val year: Int = 0,
    val period: String = "",             // "Janvier 2025"
    // Revenus
    val baseSalary: Double = 0.0,
    val overtimePay: Double = 0.0,
    val bonuses: Double = 0.0,
    val allowances: Double = 0.0,        // Indemnités
    val grossSalary: Double = 0.0,       // Salaire brut
    // Déductions
    val cnapsEmployee: Double = 0.0,     // 1% employé
    val ostieEmployee: Double = 0.0,     // 1% employé
    val incomeTax: Double = 0.0,         // IRSA
    val otherDeductions: Double = 0.0,
    val totalDeductions: Double = 0.0,
    // Net
    val netSalary: Double = 0.0,
    // Charges patronales
    val cnapsEmployer: Double = 0.0,     // 13% employeur
    val ostieEmployer: Double = 0.0,     // 5% employeur
    // Meta
    val daysWorked: Int = 0,
    val daysAbsent: Int = 0,
    val overtimeHours: Double = 0.0,
    val generatedBy: String = "",
    val generatedAt: Long = 0L,
    val isPaid: Boolean = false,
    val paidAt: Long = 0L
)