package com.orinasa.app.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.orinasa.app.model.Company
import com.orinasa.app.model.Department
import com.orinasa.app.utils.DateUtils
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val COL_COMPANIES   = "companies"
private const val COL_DEPARTMENTS = "departments"

class CompanyRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun createCompany(
        name: String, sector: String, address: String,
        phone: String, email: String, adminId: String
    ): Company {
        val ref     = db.collection(COL_COMPANIES).document()
        val code    = DateUtils.generateInviteCode()
        val company = Company(
            id         = ref.id,
            name       = name,
            sector     = sector,
            address    = address,
            phone      = phone,
            email      = email,
            inviteCode = code,
            adminId    = adminId,
            createdAt  = System.currentTimeMillis()
        )
        ref.set(company).await()
        return company
    }

    suspend fun findCompanyByInviteCode(code: String): Company? = runCatching {
        db.collection(COL_COMPANIES)
            .whereEqualTo("inviteCode", code.trim())
            .get().await()
            .documents.firstOrNull()
            ?.toObject(Company::class.java)
    }.getOrNull()

    suspend fun getCompany(companyId: String): Company? = runCatching {
        db.collection(COL_COMPANIES)
            .document(companyId)
            .get().await()
            .toObject(Company::class.java)
    }.getOrNull()

    fun listenToCompany(companyId: String): Flow<Company?> = callbackFlow {
        val l = db.collection(COL_COMPANIES)
            .document(companyId)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.toObject(Company::class.java))
            }
        awaitClose { l.remove() }
    }

    suspend fun updateCompany(companyId: String, data: Map<String, Any>) {
        db.collection(COL_COMPANIES).document(companyId).update(data).await()
    }

    suspend fun regenerateInviteCode(companyId: String): String {
        val newCode = DateUtils.generateInviteCode()
        db.collection(COL_COMPANIES)
            .document(companyId)
            .update("inviteCode", newCode).await()
        return newCode
    }

    suspend fun incrementEmployeeCount(companyId: String, by: Long = 1L) {
        db.collection(COL_COMPANIES)
            .document(companyId)
            .update("employeeCount", FieldValue.increment(by)).await()
    }

    suspend fun createDepartment(companyId: String, name: String): Department {
        val ref = db.collection(COL_COMPANIES)
            .document(companyId)
            .collection(COL_DEPARTMENTS)
            .document()
        val dept = Department(
            id        = ref.id,
            companyId = companyId,
            name      = name,
            createdAt = System.currentTimeMillis()
        )
        ref.set(dept).await()
        return dept
    }

    fun listenToDepartments(companyId: String): Flow<List<Department>> = callbackFlow {
        val l = db.collection(COL_COMPANIES)
            .document(companyId)
            .collection(COL_DEPARTMENTS)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.mapNotNull {
                    it.toObject(Department::class.java)
                } ?: emptyList())
            }
        awaitClose { l.remove() }
    }

    suspend fun deleteDepartment(companyId: String, deptId: String) {
        db.collection(COL_COMPANIES)
            .document(companyId)
            .collection(COL_DEPARTMENTS)
            .document(deptId)
            .delete().await()
    }
}