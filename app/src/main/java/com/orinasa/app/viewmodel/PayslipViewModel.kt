package com.orinasa.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.orinasa.app.model.Payslip
import com.orinasa.app.model.User
import com.orinasa.app.repository.AttendanceRepository
import com.orinasa.app.repository.PayslipRepository
import com.orinasa.app.repository.UserRepository
import com.orinasa.app.utils.DateUtils
import com.orinasa.app.utils.NotificationHelper
import com.orinasa.app.utils.PayCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PayslipUiState(
    val isLoading: Boolean      = false,
    val successMessage: String? = null,
    val errorMessage: String?   = null
)

class PayslipViewModel(
    application: Application,
    private val companyId: String,
    private val currentUser: User
) : AndroidViewModel(application) {

    private val repo           = PayslipRepository()
    private val userRepo       = UserRepository()
    private val attendanceRepo = AttendanceRepository()
    private val notifHelper    = NotificationHelper(application)

    // ── Mes fiches de paie ────────────────────────────────────────────────────
    val myPayslips: StateFlow<List<Payslip>> = repo
        .listenToUserPayslips(companyId, currentUser.userId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ── Mois sélectionné (admin) ──────────────────────────────────────────────
    private val _selectedMonth = MutableStateFlow(DateUtils.currentMonth())
    val selectedMonth: StateFlow<Int> = _selectedMonth

    private val _selectedYear = MutableStateFlow(DateUtils.currentYear())
    val selectedYear: StateFlow<Int> = _selectedYear

    // ── Fiches du mois sélectionné (admin) ────────────────────────────────────
    val monthPayslips: StateFlow<List<Payslip>> = combine(
        _selectedMonth, _selectedYear
    ) { m, y -> Pair(m, y) }
        .flatMapLatest { (m, y) -> repo.listenToMonthPayslips(companyId, m, y) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _uiState = MutableStateFlow(PayslipUiState())
    val uiState: StateFlow<PayslipUiState> = _uiState

    // ── Générer la fiche d'un employé ─────────────────────────────────────────
    fun generatePayslip(
        user: User,
        bonuses: Double        = 0.0,
        allowances: Double     = 0.0,
        otherDeductions: Double = 0.0,
        overtimeHours: Double  = 0.0
    ) = viewModelScope.launch {
        _uiState.value = PayslipUiState(isLoading = true)
        runCatching {
            val month = _selectedMonth.value
            val year  = _selectedYear.value

            // Vérifie si fiche déjà générée
            if (repo.payslipExists(companyId, user.userId, month, year)) {
                error("Une fiche de paie existe déjà pour ${user.fullName()} ce mois-ci.")
            }

            // Récupère les stats de présence du mois
            val stats          = attendanceRepo.getUserMonthlyStats(companyId, user.userId, month, year)
            val workingDays    = PayCalculator.workingDaysInMonth(month, year)
            val daysWorked     = stats["present"] ?: workingDays

            // Calcule la paie
            val payslip = PayCalculator.calculate(
                user            = user,
                month           = month,
                year            = year,
                daysWorked      = daysWorked,
                totalWorkingDays = workingDays,
                overtimeHours   = overtimeHours,
                bonuses         = bonuses,
                allowances      = allowances,
                otherDeductions = otherDeductions
            ).copy(generatedBy = currentUser.userId)

            repo.savePayslip(companyId, payslip)

            notifHelper.showNotification(
                NotificationHelper.CH_PAYSLIP,
                "Fiche de paie disponible",
                "La fiche de ${DateUtils.monthName(month, year)} de ${user.fullName()} est prête."
            )
        }.onSuccess {
            _uiState.value = PayslipUiState(
                successMessage = "Fiche de paie générée avec succès"
            )
        }.onFailure {
            _uiState.value = PayslipUiState(errorMessage = it.message ?: "Erreur")
        }
    }

    // ── Générer pour toute l'équipe ───────────────────────────────────────────
    fun generateAllPayslips(users: List<User>) = viewModelScope.launch {
        _uiState.value = PayslipUiState(isLoading = true)
        var success = 0
        var errors  = 0
        users.forEach { user ->
            runCatching {
                val month = _selectedMonth.value
                val year  = _selectedYear.value
                if (!repo.payslipExists(companyId, user.userId, month, year)) {
                    val workingDays = PayCalculator.workingDaysInMonth(month, year)
                    val stats       = attendanceRepo.getUserMonthlyStats(
                        companyId, user.userId, month, year
                    )
                    val daysWorked = stats["present"] ?: workingDays
                    val payslip    = PayCalculator.calculate(
                        user             = user,
                        month            = month,
                        year             = year,
                        daysWorked       = daysWorked,
                        totalWorkingDays = workingDays
                    ).copy(generatedBy = currentUser.userId)
                    repo.savePayslip(companyId, payslip)
                    success++
                }
            }.onFailure { errors++ }
        }
        _uiState.value = PayslipUiState(
            successMessage = "$success fiche(s) générée(s)${if (errors > 0) ", $errors erreur(s)" else ""}"
        )
    }

    // ── Marquer comme payée ───────────────────────────────────────────────────
    fun markAsPaid(payslipId: String) = viewModelScope.launch {
        runCatching { repo.markAsPaid(companyId, payslipId) }
            .onSuccess { _uiState.value = PayslipUiState(successMessage = "Marquée comme payée") }
            .onFailure { _uiState.value = PayslipUiState(errorMessage = it.message) }
    }

    fun setMonth(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value  = year
    }

    fun clearMessage() { _uiState.value = PayslipUiState() }
}