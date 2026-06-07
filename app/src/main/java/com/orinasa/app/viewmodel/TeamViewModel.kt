package com.orinasa.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orinasa.app.model.Department
import com.orinasa.app.model.User
import com.orinasa.app.model.WorkSchedule
import com.orinasa.app.repository.CompanyRepository
import com.orinasa.app.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TeamUiState(
    val isLoading: Boolean      = false,
    val successMessage: String? = null,
    val errorMessage: String?   = null
)

class TeamViewModel(
    private val companyId: String
) : ViewModel() {

    private val userRepo    = UserRepository()
    private val companyRepo = CompanyRepository()

    // ── Tous les employés actifs ──────────────────────────────────────────────
    val users: StateFlow<List<User>> = userRepo
        .listenToUsers(companyId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ── Départements ──────────────────────────────────────────────────────────
    val departments: StateFlow<List<Department>> = companyRepo
        .listenToDepartments(companyId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _uiState = MutableStateFlow(TeamUiState())
    val uiState: StateFlow<TeamUiState> = _uiState

    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser: StateFlow<User?> = _selectedUser

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // ── Résultats filtrés ─────────────────────────────────────────────────────
    val filteredUsers: StateFlow<List<User>> = combine(
        users, _searchQuery
    ) { list, query ->
        if (query.isBlank()) list
        else list.filter {
            it.fullName().contains(query, ignoreCase = true) ||
                    it.position.contains(query, ignoreCase = true)  ||
                    it.departmentName.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ── Mettre à jour un employé ──────────────────────────────────────────────
    fun updateUser(
        userId: String,
        firstName: String,
        lastName: String,
        position: String,
        departmentId: String,
        departmentName: String,
        baseSalary: Double,
        contractType: String,
        workSchedule: WorkSchedule,
        phone: String,
        startDate: String
    ) = viewModelScope.launch {
        _uiState.value = TeamUiState(isLoading = true)
        runCatching {
            userRepo.updateUser(companyId, userId, mapOf(
                "firstName"      to firstName.trim(),
                "lastName"       to lastName.trim(),
                "position"       to position.trim(),
                "departmentId"   to departmentId,
                "departmentName" to departmentName,
                "baseSalary"     to baseSalary,
                "contractType"   to contractType,
                "workSchedule"   to mapOf(
                    "startTime"           to workSchedule.startTime,
                    "endTime"             to workSchedule.endTime,
                    "workDays"            to workSchedule.workDays,
                    "lateToleranceMinutes" to workSchedule.lateToleranceMinutes
                ),
                "phone"     to phone.trim(),
                "startDate" to startDate
            ))
        }.onSuccess {
            _uiState.value = TeamUiState(successMessage = "Profil mis à jour")
        }.onFailure {
            _uiState.value = TeamUiState(errorMessage = it.message ?: "Erreur")
        }
    }

    // ── Désactiver un employé ─────────────────────────────────────────────────
    fun deactivateUser(userId: String) = viewModelScope.launch {
        _uiState.value = TeamUiState(isLoading = true)
        runCatching { userRepo.deactivateUser(companyId, userId) }
            .onSuccess { _uiState.value = TeamUiState(successMessage = "Employé archivé") }
            .onFailure { _uiState.value = TeamUiState(errorMessage = it.message) }
    }

    // ── Réactiver un employé ──────────────────────────────────────────────────
    fun reactivateUser(userId: String) = viewModelScope.launch {
        runCatching { userRepo.reactivateUser(companyId, userId) }
            .onSuccess { _uiState.value = TeamUiState(successMessage = "Employé réactivé") }
            .onFailure { _uiState.value = TeamUiState(errorMessage = it.message) }
    }

    // ── Créer un département ──────────────────────────────────────────────────
    fun createDepartment(name: String) = viewModelScope.launch {
        if (name.isBlank()) {
            _uiState.value = TeamUiState(errorMessage = "Nom du département vide")
            return@launch
        }
        runCatching { companyRepo.createDepartment(companyId, name.trim()) }
            .onSuccess { _uiState.value = TeamUiState(successMessage = "Département créé") }
            .onFailure { _uiState.value = TeamUiState(errorMessage = it.message) }
    }

    // ── Supprimer un département ──────────────────────────────────────────────
    fun deleteDepartment(deptId: String) = viewModelScope.launch {
        runCatching { companyRepo.deleteDepartment(companyId, deptId) }
            .onSuccess { _uiState.value = TeamUiState(successMessage = "Département supprimé") }
            .onFailure { _uiState.value = TeamUiState(errorMessage = it.message) }
    }

    fun selectUser(user: User) { _selectedUser.value = user }
    fun clearSelectedUser()    { _selectedUser.value = null }
    fun setSearch(q: String)   { _searchQuery.value  = q }
    fun clearMessage()         { _uiState.value = TeamUiState() }
}