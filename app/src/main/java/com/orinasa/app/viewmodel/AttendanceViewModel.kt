package com.orinasa.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.orinasa.app.model.Attendance
import com.orinasa.app.model.User
import com.orinasa.app.repository.AttendanceRepository
import com.orinasa.app.repository.UserRepository
import com.orinasa.app.utils.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AttendanceUiState(
    val isLoading: Boolean      = false,
    val successMessage: String? = null,
    val errorMessage: String?   = null
)

class AttendanceViewModel(
    application: Application,
    private val companyId: String,
    private val currentUser: User
) : AndroidViewModel(application) {

    private val repo     = AttendanceRepository()
    private val userRepo = UserRepository()

    // ── Pointage du jour de l'utilisateur ─────────────────────────────────────
    val todayAttendance: StateFlow<Attendance?> = repo
        .listenTodayAttendance(companyId, currentUser.userId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // ── Historique personnel ──────────────────────────────────────────────────
    val myHistory: StateFlow<List<Attendance>> = repo
        .listenToUserAttendance(companyId, currentUser.userId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ── Pointage équipe aujourd'hui (admin) ───────────────────────────────────
    val teamToday: StateFlow<List<Attendance>> = repo
        .listenTeamAttendanceToday(companyId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ── Tous les employés (pour vue admin) ────────────────────────────────────
    val allUsers: StateFlow<List<User>> = userRepo
        .listenToUsers(companyId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _uiState = MutableStateFlow(AttendanceUiState())
    val uiState: StateFlow<AttendanceUiState> = _uiState

    private val _selectedMonth = MutableStateFlow(DateUtils.currentMonth())
    val selectedMonth: StateFlow<Int> = _selectedMonth

    private val _selectedYear = MutableStateFlow(DateUtils.currentYear())
    val selectedYear: StateFlow<Int> = _selectedYear

    private val _monthlyStats = MutableStateFlow<Map<String, Int>>(emptyMap())
    val monthlyStats: StateFlow<Map<String, Int>> = _monthlyStats

    // ── Pointer l'arrivée ─────────────────────────────────────────────────────
    fun clockIn() = viewModelScope.launch {
        _uiState.value = AttendanceUiState(isLoading = true)
        runCatching {
            val schedule = currentUser.workSchedule
            repo.clockIn(
                companyId      = companyId,
                userId         = currentUser.userId,
                userFullName   = currentUser.fullName(),
                userPhotoUrl   = currentUser.photoUrl,
                departmentName = currentUser.departmentName,
                scheduledStart = schedule.startTime,
                toleranceMinutes = schedule.lateToleranceMinutes
            )
        }.onSuccess { attendance ->
            val msg = if (attendance.isLate)
                "Pointage enregistré — Retard de ${attendance.lateMinutes} min"
            else "Bonne journée ! Pointage à ${attendance.clockInTime}"
            _uiState.value = AttendanceUiState(successMessage = msg)
        }.onFailure {
            _uiState.value = AttendanceUiState(errorMessage = "Erreur : ${it.message}")
        }
    }

    // ── Pointer la sortie ─────────────────────────────────────────────────────
    fun clockOut() = viewModelScope.launch {
        _uiState.value = AttendanceUiState(isLoading = true)
        runCatching {
            repo.clockOut(companyId, currentUser.userId)
        }.onSuccess { attendance ->
            val hours   = (attendance?.workDurationMinutes ?: 0) / 60
            val minutes = (attendance?.workDurationMinutes ?: 0) % 60
            _uiState.value = AttendanceUiState(
                successMessage = "Bonne soirée ! Durée : ${hours}h${minutes}min"
            )
        }.onFailure {
            _uiState.value = AttendanceUiState(errorMessage = "Erreur : ${it.message}")
        }
    }

    // ── Charger les stats du mois ─────────────────────────────────────────────
    fun loadMonthlyStats(userId: String = currentUser.userId) = viewModelScope.launch {
        val stats = repo.getUserMonthlyStats(
            companyId, userId,
            _selectedMonth.value, _selectedYear.value
        )
        _monthlyStats.value = stats
    }

    // ── Marquer absent (admin) ────────────────────────────────────────────────
    fun markAbsent(userId: String, userFullName: String, note: String = "") =
        viewModelScope.launch {
            runCatching {
                repo.markAbsent(companyId, userId, userFullName, note = note)
            }.onSuccess {
                _uiState.value = AttendanceUiState(successMessage = "Absence enregistrée")
            }.onFailure {
                _uiState.value = AttendanceUiState(errorMessage = "Erreur : ${it.message}")
            }
        }

    fun setMonth(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value  = year
        loadMonthlyStats()
    }

    fun clearMessage() {
        _uiState.value = AttendanceUiState()
    }
}