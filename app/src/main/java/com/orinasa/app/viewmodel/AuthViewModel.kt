package com.orinasa.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.orinasa.app.model.Company
import com.orinasa.app.model.User
import com.orinasa.app.repository.AuthRepository
import com.orinasa.app.repository.CompanyRepository
import com.orinasa.app.repository.UserRepository
import com.orinasa.app.utils.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Idle        : AuthState()
    object Loading     : AuthState()
    object Success     : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepo    = AuthRepository()
    private val companyRepo = CompanyRepository()
    private val settings    = SettingsRepository(application)

    val isLoggedIn get() = authRepo.isLoggedIn
    val currentUid get() = authRepo.currentUid

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _company = MutableStateFlow<Company?>(null)
    val company: StateFlow<Company?> = _company

    val companyId = settings.companyId
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val userId = settings.userId
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val isAdmin = settings.isAdmin
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // ── Connexion ─────────────────────────────────────────────────────────────
    fun login(email: String, password: String) = viewModelScope.launch {
        _state.value = AuthState.Loading
        runCatching {
            authRepo.login(email.trim(), password.trim())
            val uid       = authRepo.currentUid
            val cId       = authRepo.findCompanyIdForUser(uid)
                ?: error("Entreprise introuvable pour cet utilisateur.")
            val user      = authRepo.fetchCurrentUser(cId)
                ?: error("Profil introuvable. Contactez votre administrateur.")
            if (!user.isActive) error("Ce compte a été désactivé. Contactez votre administrateur.")
            val company   = companyRepo.getCompany(cId)
                ?: error("Entreprise introuvable.")
            settings.saveSession(cId, uid, user.isAdmin)
            _currentUser.value = user
            _company.value     = company
            _state.value       = AuthState.Success
        }.onFailure {
            _state.value = AuthState.Error(friendlyError(it.message))
        }
    }

    // ── Créer une entreprise + compte admin ───────────────────────────────────
    fun registerCompany(
        companyName: String,
        sector: String,
        address: String,
        companyPhone: String,
        companyEmail: String,
        adminFirstName: String,
        adminLastName: String,
        adminEmail: String,
        adminPassword: String
    ) = viewModelScope.launch {
        _state.value = AuthState.Loading
        runCatching {
            // 1. Crée le compte Auth (juste pour obtenir l'UID)
            val tempUid = authRepo.register(
                email     = adminEmail.trim(),
                password  = adminPassword,
                firstName = adminFirstName.trim(),
                lastName  = adminLastName.trim(),
                companyId = "temp",  // sera mis à jour
                isAdmin   = true
            )
            // 2. Crée l'entreprise
            val company = companyRepo.createCompany(
                name     = companyName.trim(),
                sector   = sector,
                address  = address.trim(),
                phone    = companyPhone.trim(),
                email    = companyEmail.trim(),
                adminId  = tempUid
            )
            // 3. Met à jour le profil user avec le bon companyId
            val userRepo = com.orinasa.app.repository.UserRepository()
            userRepo.updateUser(company.id, tempUid, mapOf("companyId" to company.id))

            // 4. Incrément count
            companyRepo.incrementEmployeeCount(company.id)

            settings.saveSession(company.id, tempUid, true)
            _company.value     = company
            _currentUser.value = authRepo.fetchCurrentUser(company.id)
            _state.value       = AuthState.Success
        }.onFailure {
            _state.value = AuthState.Error(friendlyError(it.message))
        }
    }

    // ── Rejoindre une entreprise ──────────────────────────────────────────────
    fun joinCompany(
        inviteCode: String,
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ) = viewModelScope.launch {
        _state.value = AuthState.Loading
        runCatching {
            val company = companyRepo.findCompanyByInviteCode(inviteCode.trim())
                ?: error("Code d'invitation invalide ou expiré.")

            val uid = authRepo.register(
                email     = email.trim(),
                password  = password,
                firstName = firstName.trim(),
                lastName  = lastName.trim(),
                companyId = company.id,
                isAdmin   = false
            )
            companyRepo.incrementEmployeeCount(company.id)
            settings.saveSession(company.id, uid, false)
            _company.value     = company
            _currentUser.value = authRepo.fetchCurrentUser(company.id)
            _state.value       = AuthState.Success
        }.onFailure {
            _state.value = AuthState.Error(friendlyError(it.message))
        }
    }

    // ── Changer email ─────────────────────────────────────────────────────────
    fun updateEmail(newEmail: String, password: String, onDone: (Boolean, String?) -> Unit) =
        viewModelScope.launch {
            runCatching { authRepo.updateEmail(newEmail, password) }
                .onSuccess { onDone(true, null) }
                .onFailure { onDone(false, friendlyError(it.message)) }
        }

    // ── Changer mot de passe ──────────────────────────────────────────────────
    fun updatePassword(current: String, newPwd: String, onDone: (Boolean, String?) -> Unit) =
        viewModelScope.launch {
            runCatching { authRepo.updatePassword(current, newPwd) }
                .onSuccess { onDone(true, null) }
                .onFailure { onDone(false, friendlyError(it.message)) }
        }

    // ── Mot de passe oublié ───────────────────────────────────────────────────
    fun resetPassword(email: String, onDone: (Boolean, String?) -> Unit) =
        viewModelScope.launch {
            runCatching { authRepo.resetPassword(email.trim()) }
                .onSuccess { onDone(true, null) }
                .onFailure { onDone(false, friendlyError(it.message)) }
        }

    // ── Créer un employé directement (admin) ─────────────────────────────────────
    fun registerEmployeeByAdmin(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        position: String,
        departmentId: String,
        departmentName: String,
        baseSalary: Double,
        contractType: String,
        companyId: String,
        onDone: (Boolean, String?) -> Unit
    ) = viewModelScope.launch {
        runCatching {
            // Crée le compte via une instance Firebase secondaire
            // pour ne pas déconnecter l'admin
            val options = com.google.firebase.FirebaseOptions.Builder()
                .setApplicationId(
                    com.google.firebase.FirebaseApp.getInstance().options.applicationId
                )
                .setApiKey(
                    com.google.firebase.FirebaseApp.getInstance().options.apiKey ?: ""
                )
                .setProjectId(
                    com.google.firebase.FirebaseApp.getInstance().options.projectId ?: ""
                )
                .build()

            val appName     = "secondary_${System.currentTimeMillis()}"
            val secondaryApp = com.google.firebase.FirebaseApp.initializeApp(
                getApplication(), options, appName
            )
            val secondaryAuth = com.google.firebase.auth.FirebaseAuth
                .getInstance(secondaryApp)

            val result = secondaryAuth
                .createUserWithEmailAndPassword(email.trim(), password)
                .await()
            val newUid = result.user?.uid ?: error("UID null")
            secondaryAuth.signOut()
            secondaryApp.delete()

            // Crée le profil dans Firestore
            val userRepo = UserRepository()
            userRepo.addUser(
                com.orinasa.app.model.User(
                    userId         = newUid,
                    companyId      = companyId,
                    email          = email.trim(),
                    firstName      = firstName.trim(),
                    lastName       = lastName.trim(),
                    position       = position.trim(),
                    departmentId   = departmentId,
                    departmentName = departmentName,
                    baseSalary     = baseSalary,
                    contractType   = contractType,
                    isAdmin        = false,
                    isActive       = true,
                    createdAt      = System.currentTimeMillis(),
                    updatedAt      = System.currentTimeMillis()
                )
            )
            companyRepo.incrementEmployeeCount(companyId)
        }.onSuccess {
            onDone(true, null)
        }.onFailure { e ->
            onDone(false, when {
                "email" in (e.message ?: "").lowercase()   -> "Email déjà utilisé"
                "password" in (e.message ?: "").lowercase() -> "Mot de passe trop faible"
                else -> e.message ?: "Erreur inconnue"
            })
        }
    }

    // ── Déconnexion ───────────────────────────────────────────────────────────
    fun logout() = viewModelScope.launch {
        authRepo.logout()
        settings.clearSession()
        _currentUser.value = null
        _company.value     = null
        _state.value       = AuthState.Idle
    }

    // ── Supprimer le compte ───────────────────────────────────────────────────
    fun deleteAccount(password: String, onDone: (Boolean, String?) -> Unit) =
        viewModelScope.launch {
            _state.value = AuthState.Loading
            runCatching {
                val cId = companyId.value
                authRepo.deleteAccount(cId, password)
                settings.clearSession()
            }.onSuccess {
                _currentUser.value = null
                _company.value     = null
                _state.value       = AuthState.Idle
                onDone(true, null)
            }.onFailure {
                _state.value = AuthState.Idle
                onDone(false, friendlyError(it.message))
            }
        }

    fun resetState() { _state.value = AuthState.Idle }

    private fun friendlyError(msg: String?) = when {
        msg == null                           -> "Erreur inconnue"
        "password" in msg.lowercase()         -> "Mot de passe incorrect"
        "email" in msg.lowercase()            -> "Email invalide"
        "already" in msg.lowercase()          -> "Email déjà utilisé"
        "no user" in msg.lowercase()          -> "Aucun compte trouvé"
        "network" in msg.lowercase()          -> "Vérifiez votre connexion"
        "invalid" in msg.lowercase()          -> "Email ou mot de passe invalide"
        "weak" in msg.lowercase()             -> "Mot de passe trop faible (6 min)"
        "Entreprise introuvable" in msg       -> msg
        "Profil introuvable" in msg           -> msg
        "désactivé" in msg                    -> msg
        "Code d'invitation" in msg            -> msg
        else                                  -> msg
    }
}
