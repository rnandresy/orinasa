package com.orinasa.app.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.orinasa.app.model.User
import kotlinx.coroutines.tasks.await

private const val COL_COMPANIES = "companies"
private const val COL_USERS     = "users"

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    val currentUser get() = auth.currentUser
    val currentUid  get() = auth.currentUser?.uid ?: ""
    val isLoggedIn  get() = auth.currentUser != null

    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        companyId: String,
        isAdmin: Boolean = false
    ): String {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid    = result.user?.uid ?: error("UID null après inscription")

        val user = User(
            userId    = uid,
            companyId = companyId,
            email     = email,
            firstName = firstName,
            lastName  = lastName,
            isAdmin   = isAdmin,
            isActive  = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        db.collection(COL_COMPANIES)
            .document(companyId)
            .collection(COL_USERS)
            .document(uid)
            .set(user)
            .await()

        return uid
    }

    suspend fun fetchCurrentUser(companyId: String): User? = runCatching {
        db.collection(COL_COMPANIES)
            .document(companyId)
            .collection(COL_USERS)
            .document(currentUid)
            .get().await()
            .toObject(User::class.java)
    }.getOrNull()

    suspend fun findCompanyIdForUser(uid: String): String? = runCatching {
        val companies = db.collection(COL_COMPANIES).get().await()
        for (company in companies.documents) {
            val exists = company.reference
                .collection(COL_USERS)
                .document(uid)
                .get().await()
                .exists()
            if (exists) return@runCatching company.id
        }
        null
    }.getOrNull()

    suspend fun reauthenticate(password: String) {
        val user = auth.currentUser ?: error("Non connecté")
        val cred = EmailAuthProvider.getCredential(user.email ?: "", password)
        user.reauthenticate(cred).await()
    }

    suspend fun updateEmail(newEmail: String, password: String) {
        reauthenticate(password)
        auth.currentUser?.verifyBeforeUpdateEmail(newEmail)?.await()
    }

    suspend fun updatePassword(currentPwd: String, newPwd: String) {
        reauthenticate(currentPwd)
        auth.currentUser?.updatePassword(newPwd)?.await()
    }

    suspend fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    fun logout() = auth.signOut()

    suspend fun deleteAccount(companyId: String, password: String) {
        reauthenticate(password)
        db.collection(COL_COMPANIES)
            .document(companyId)
            .collection(COL_USERS)
            .document(currentUid)
            .delete().await()
        auth.currentUser?.delete()?.await()
    }
}