package com.orinasa.app.utils

import com.orinasa.app.model.Payslip
import com.orinasa.app.model.User

// Taux cotisations locaux — pas d'import externe
private const val CNAPS_EMP  = 0.01
private const val CNAPS_EMPR = 0.13
private const val OSTIE_EMP  = 0.01
private const val OSTIE_EMPR = 0.05

object PayCalculator {

    fun calculate(
        user: User,
        month: Int,
        year: Int,
        daysWorked: Int,
        totalWorkingDays: Int,
        overtimeHours: Double   = 0.0,
        bonuses: Double         = 0.0,
        allowances: Double      = 0.0,
        otherDeductions: Double = 0.0
    ): Payslip {
        val base       = user.baseSalary
        val daysAbsent = totalWorkingDays - daysWorked

        val proRated    = if (totalWorkingDays > 0)
            base * daysWorked.toDouble() / totalWorkingDays.toDouble() else base

        val hourlyRate  = base / (totalWorkingDays.coerceAtLeast(1).toDouble() * 8.0)
        val overtime    = overtimeHours * hourlyRate * 1.5
        val gross       = proRated + overtime + bonuses + allowances

        val cnapsE      = gross * CNAPS_EMP
        val ostieE      = gross * OSTIE_EMP
        val irsa        = calculateIRSA(gross - cnapsE - ostieE)
        val totalDed    = cnapsE + ostieE + irsa + otherDeductions
        val net         = gross - totalDed
        val cnapsEr     = gross * CNAPS_EMPR
        val ostieEr     = gross * OSTIE_EMPR

        return Payslip(
            userId          = user.userId,
            companyId       = user.companyId,
            userFullName    = user.fullName(),
            position        = user.position,
            month           = month,
            year            = year,
            period          = DateUtils.monthName(month, year),
            baseSalary      = base,
            overtimePay     = overtime,
            bonuses         = bonuses,
            allowances      = allowances,
            grossSalary     = gross,
            cnapsEmployee   = cnapsE,
            ostieEmployee   = ostieE,
            incomeTax       = irsa,
            otherDeductions = otherDeductions,
            totalDeductions = totalDed,
            netSalary       = net,
            cnapsEmployer   = cnapsEr,
            ostieEmployer   = ostieEr,
            daysWorked      = daysWorked,
            daysAbsent      = daysAbsent,
            overtimeHours   = overtimeHours,
            generatedAt     = System.currentTimeMillis()
        )
    }

    private fun calculateIRSA(taxableIncome: Double): Double {
        for ((low, high, rate) in IRSA_BRACKETS) {
            if (taxableIncome in low..high) return taxableIncome * rate
        }
        return taxableIncome * 0.20
    }

    fun workingDaysInMonth(month: Int, year: Int): Int {
        val cal         = java.util.Calendar.getInstance()
        cal.set(year, month - 1, 1)
        val daysInMonth = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        var count       = 0
        for (day in 1..daysInMonth) {
            cal.set(year, month - 1, day)
            val dow = cal.get(java.util.Calendar.DAY_OF_WEEK)
            if (dow != java.util.Calendar.SATURDAY && dow != java.util.Calendar.SUNDAY)
                count++
        }
        return count
    }
}