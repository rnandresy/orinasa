package com.orinasa.app.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.orinasa.app.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val COL_COMPANIES = "companies"
private const val COL_USERS     = "users"

class UserRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun usersRef(companyId: String) =
        db.collection(COL_COMPANIES)
            .document(companyId)
            .collection(COL_USERS)

    fun listenToUsers(companyId: String): Flow<List<User>> = callbackFlow {
        val l = usersRef(companyId)
            .whereEqualTo("isActive", true)
            .orderBy("firstName", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.mapNotNull {
                    it.toObject(User::class.java)
                } ?: emptyList())
            }
        awaitClose { l.remove() }
    }

    fun listenToUser(companyId: String, userId: String): Flow<User?> = callbackFlow {
        val l = usersRef(companyId).document(userId)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.toObject(User::class.java))
            }
        awaitClose { l.remove() }
    }

    suspend fun getUser(companyId: String, userId: String): User? = runCatching {
        usersRef(companyId).document(userId).get().await()
            .toObject(User::class.java)
    }.getOrNull()

    suspend fun getAllUsers(companyId: String): List<User> = runCatching {
        usersRef(companyId)
            .whereEqualTo("isActive", true)
            .get().await()
            .documents.mapNotNull { it.toObject(User::class.java) }
    }.getOrElse { emptyList() }

    suspend fun addUser(user: User) {
        usersRef(user.companyId)
            .document(user.userId)
            .set(user).await()
    }

    suspend fun updateUser(companyId: String, userId: String, data: Map<String, Any>) {
        val updates = data.toMutableMap()
        updates["updatedAt"] = System.currentTimeMillis()
        usersRef(companyId).document(userId).update(updates).await()
    }

    suspend fun deactivateUser(companyId: String, userId: String) {
        usersRef(companyId).document(userId).update(
            "isActive",  false,
            "updatedAt", System.currentTimeMillis()
        ).await()
    }

    suspend fun reactivateUser(companyId: String, userId: String) {
        usersRef(companyId).document(userId).update(
            "isActive",  true,
            "updatedAt", System.currentTimeMillis()
        ).await()
    }

    suspend fun updateLeaveBalance(companyId: String, userId: String, used: Int) {
        usersRef(companyId).document(userId)
            .update("leaveUsed", FieldValue.increment(used.toLong()))
            .await()
    }

    fun listenToUsersByDepartment(
        companyId: String, departmentId: String
    ): Flow<List<User>> = callbackFlow {
        val l = usersRef(companyId)
            .whereEqualTo("departmentId", departmentId)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.mapNotNull {
                    it.toObject(User::class.java)
                } ?: emptyList())
            }
        awaitClose { l.remove() }
    }
}