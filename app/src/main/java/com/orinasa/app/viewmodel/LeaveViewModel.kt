package com.orinasa.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orinasa.app.model.LeaveRequest
import com.orinasa.app.model.LeaveStatus
import com.orinasa.app.model.LeaveType
import com.orinasa.app.model.User
import com.orinasa.app.repository.LeaveRepository
import com.orinasa.app.repository.UserRepository
import com.orinasa.app.utils.NotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LeaveUiState(
    val isLoading: Boolean      = false,
    val successMessage: String? = null,
    val errorMessage: String?   = null
)

class LeaveViewModel(
    private val companyId: String,
    private val currentUser: User,
    private val notifHelper: NotificationHelper
) : ViewModel() {

    private val repo     = LeaveRepository()
    private val userRepo = UserRepository()

    // ── Mes demandes ──────────────────────────────────────────────────────────
    val myLeaves: StateFlow<List<LeaveRequest>> = repo
        .listenToUserLeaves(companyId, currentUser.userId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ── Toutes les demandes (admin) ───────────────────────────────────────────
    val allLeaves: StateFlow<List<LeaveRequest>> = repo
        .listenToAllLeaves(companyId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ── Demandes en attente (admin) ───────────────────────────────────────────
    val pendingLeaves: StateFlow<List<LeaveRequest>> = repo
        .listenToPendingLeaves(companyId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _uiState = MutableStateFlow(LeaveUiState())
    val uiState: StateFlow<LeaveUiState> = _uiState

    // ── Solde calculé ─────────────────────────────────────────────────────────
    val leaveBalance: StateFlow<Int> = MutableStateFlow(currentUser.leaveRemaining())

    // ── Soumettre une demande ─────────────────────────────────────────────────
    fun submitRequest(
        type: LeaveType,
        startDate: String,
        endDate: String,
        reason: String
    ) = viewModelScope.launch {
        _uiState.value = LeaveUiState(isLoading = true)
        runCatching {
            // Vérifie chevauchement
            val overlaps = repo.getActiveLeavesBetween(
                companyId, currentUser.userId, startDate, endDate
            )
            if (overlaps.isNotEmpty()) error("Vous avez déjà une demande sur ces dates.")

            // Vérifie solde pour congé annuel
            val daysReq = com.orinasa.app.utils.DateUtils.daysBetween(startDate, endDate)
            if (type == LeaveType.ANNUAL && daysReq > currentUser.leaveRemaining()) {
                error("Solde insuffisant. Reste : ${currentUser.leaveRemaining()} jour(s).")
            }

            repo.submitLeaveRequest(
                companyId      = companyId,
                userId         = currentUser.userId,
                userFullName   = currentUser.fullName(),
                userPhotoUrl   = currentUser.photoUrl,
                departmentName = currentUser.departmentName,
                type           = type,
                startDate      = startDate,
                endDate        = endDate,
                reason         = reason
            )
        }.onSuccess {
            _uiState.value = LeaveUiState(successMessage = "Demande envoyée avec succès")
        }.onFailure {
            _uiState.value = LeaveUiState(errorMessage = it.message ?: "Erreur")
        }
    }

    // ── Annuler sa demande ────────────────────────────────────────────────────
    fun cancelRequest(leaveId: String) = viewModelScope.launch {
        runCatching { repo.cancelLeave(companyId, leaveId) }
            .onSuccess { _uiState.value = LeaveUiState(successMessage = "Demande annulée") }
            .onFailure { _uiState.value = LeaveUiState(errorMessage = it.message) }
    }

    // ── Approuver (admin) ─────────────────────────────────────────────────────
    fun approveLeave(leaveId: String, note: String = "") = viewModelScope.launch {
        _uiState.value = LeaveUiState(isLoading = true)
        runCatching {
            val leave = allLeaves.value.find { it.id == leaveId }
                ?: error("Demande introuvable")

            repo.approveLeave(
                companyId      = companyId,
                leaveId        = leaveId,
                reviewedById   = currentUser.userId,
                reviewedByName = currentUser.fullName(),
                note           = note
            )

            // Débite le solde de congé annuel
            if (leave.type == LeaveType.ANNUAL) {
                userRepo.updateLeaveBalance(companyId, leave.userId, leave.daysCount)
            }

            // Notification système
            notifHelper.showNotification(
                NotificationHelper.CH_LEAVE,
                "Congé approuvé",
                "La demande de ${leave.userFullName} a été approuvée."
            )
        }.onSuccess {
            _uiState.value = LeaveUiState(successMessage = "Demande approuvée")
        }.onFailure {
            _uiState.value = LeaveUiState(errorMessage = it.message ?: "Erreur")
        }
    }

    // ── Refuser (admin) ───────────────────────────────────────────────────────
    fun rejectLeave(leaveId: String, note: String) = viewModelScope.launch {
        if (note.isBlank()) {
            _uiState.value = LeaveUiState(errorMessage = "Veuillez préciser le motif du refus")
            return@launch
        }
        _uiState.value = LeaveUiState(isLoading = true)
        runCatching {
            val leave = allLeaves.value.find { it.id == leaveId }
            repo.rejectLeave(
                companyId      = companyId,
                leaveId        = leaveId,
                reviewedById   = currentUser.userId,
                reviewedByName = currentUser.fullName(),
                note           = note
            )
            leave?.let {
                notifHelper.showNotification(
                    NotificationHelper.CH_LEAVE,
                    "Congé refusé",
                    "La demande de ${it.userFullName} a été refusée."
                )
            }
        }.onSuccess {
            _uiState.value = LeaveUiState(successMessage = "Demande refusée")
        }.onFailure {
            _uiState.value = LeaveUiState(errorMessage = it.message ?: "Erreur")
        }
    }

    fun clearMessage() { _uiState.value = LeaveUiState() }
}