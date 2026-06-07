package com.orinasa.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orinasa.app.model.*
import com.orinasa.app.repository.AnnouncementRepository
import com.orinasa.app.repository.*
import com.orinasa.app.utils.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardStats(
    val totalEmployees: Int   = 0,
    val presentToday: Int     = 0,
    val absentToday: Int      = 0,
    val lateToday: Int        = 0,
    val onLeaveToday: Int     = 0,
    val pendingLeaves: Int    = 0
)

class DashboardViewModel(
    private val companyId: String,
    private val userId: String,
    private val isAdmin: Boolean
) : ViewModel() {

    private val userRepo       = UserRepository()
    private val attendanceRepo = AttendanceRepository()
    private val leaveRepo      = LeaveRepository()
    private val announcRepo    = AnnouncementRepository(companyId)

    // ── Employés ──────────────────────────────────────────────────────────────
    val allUsers: StateFlow<List<User>> = userRepo
        .listenToUsers(companyId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ── Pointage aujourd'hui ──────────────────────────────────────────────────
    val teamAttendanceToday: StateFlow<List<Attendance>> = attendanceRepo
        .listenTeamAttendanceToday(companyId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ── Mon pointage aujourd'hui ──────────────────────────────────────────────
    val myAttendanceToday: StateFlow<Attendance?> = attendanceRepo
        .listenTodayAttendance(companyId, userId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // ── Demandes en attente ───────────────────────────────────────────────────
    val pendingLeaves: StateFlow<List<LeaveRequest>> = leaveRepo
        .listenToPendingLeaves(companyId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ── Annonces récentes ─────────────────────────────────────────────────────
    val announcements: StateFlow<List<Announcement>> = announcRepo
        .listenToAnnouncements()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ── Statistiques calculées ────────────────────────────────────────────────
    val stats: StateFlow<DashboardStats> = combine(
        allUsers, teamAttendanceToday, pendingLeaves
    ) { users, attendance, pending ->
        val present  = attendance.count {
            it.status == AttendanceStatus.PRESENT || it.status == AttendanceStatus.LATE
        }
        val absent   = attendance.count { it.status == AttendanceStatus.ABSENT }
        val late     = attendance.count { it.isLate }
        val onLeave  = attendance.count { it.status == AttendanceStatus.ON_LEAVE }

        DashboardStats(
            totalEmployees = users.size,
            presentToday   = present,
            absentToday    = absent,
            lateToday      = late,
            onLeaveToday   = onLeave,
            pendingLeaves  = pending.size
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, DashboardStats())

    // ── Annonce marquée comme lue ─────────────────────────────────────────────
    fun markAnnouncementRead(announcementId: String) = viewModelScope.launch {
        runCatching { announcRepo.markAsRead(announcementId, userId) }
    }
}