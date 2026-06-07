package com.orinasa.app.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.orinasa.app.model.User
import com.orinasa.app.repository.UserRepository
import com.orinasa.app.utils.CloudinaryUploader
import com.orinasa.app.utils.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean      = false,
    val uploadProgress: Int     = 0,
    val successMessage: String? = null,
    val errorMessage: String?   = null
)

class ProfileViewModel(
    application: Application,
    private val companyId: String,
    private val userId: String
) : AndroidViewModel(application) {

    private val repo     = UserRepository()
    private val settings = SettingsRepository(application)

    // ── Profil en temps réel ──────────────────────────────────────────────────
    val profile: StateFlow<User?> = repo
        .listenToUser(companyId, userId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    // ── Mettre à jour le profil ───────────────────────────────────────────────
    fun updateProfile(
        firstName: String,
        lastName: String,
        phone: String
    ) = viewModelScope.launch {
        if (firstName.isBlank() || lastName.isBlank()) {
            _uiState.value = ProfileUiState(errorMessage = "Le nom et prénom sont requis")
            return@launch
        }
        _uiState.value = ProfileUiState(isLoading = true)
        runCatching {
            repo.updateUser(companyId, userId, mapOf(
                "firstName" to firstName.trim(),
                "lastName"  to lastName.trim(),
                "phone"     to phone.trim()
            ))
        }.onSuccess {
            _uiState.value = ProfileUiState(successMessage = "Profil mis à jour")
        }.onFailure {
            _uiState.value = ProfileUiState(errorMessage = it.message ?: "Erreur")
        }
    }

    // ── Upload photo de profil ────────────────────────────────────────────────
    fun uploadPhoto(uri: Uri) = viewModelScope.launch {
        _uiState.value = ProfileUiState(isLoading = true, uploadProgress = 0)
        runCatching {
            val url = CloudinaryUploader.uploadImage(
                context    = getApplication(),
                uri        = uri,
                onProgress = { _uiState.value = ProfileUiState(isLoading = true, uploadProgress = it) }
            )
            repo.updateUser(companyId, userId, mapOf("photoUrl" to url))
        }.onSuccess {
            _uiState.value = ProfileUiState(successMessage = "Photo mise à jour")
        }.onFailure {
            _uiState.value = ProfileUiState(errorMessage = "Erreur upload : ${it.message}")
        }
    }

    // ── Supprimer la photo ────────────────────────────────────────────────────
    fun deletePhoto() = viewModelScope.launch {
        runCatching { repo.updateUser(companyId, userId, mapOf("photoUrl" to "")) }
            .onSuccess { _uiState.value = ProfileUiState(successMessage = "Photo supprimée") }
            .onFailure { _uiState.value = ProfileUiState(errorMessage = it.message) }
    }

    // ── Paramètres notifications ───────────────────────────────────────────────
    val notifyLeave = settings.notifyLeave.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val notifyPay   = settings.notifyPay.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val notifyAnn   = settings.notifyAnn.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun setNotifyLeave(v: Boolean) = viewModelScope.launch { settings.setNotifyLeave(v) }
    fun setNotifyPay(v: Boolean)   = viewModelScope.launch { settings.setNotifyPay(v) }
    fun setNotifyAnn(v: Boolean)   = viewModelScope.launch { settings.setNotifyAnn(v) }

    fun clearMessage() { _uiState.value = ProfileUiState() }
}