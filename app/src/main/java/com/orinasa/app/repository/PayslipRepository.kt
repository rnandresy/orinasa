package com.orinasa.app.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.orinasa.app.model.Payslip
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val COL_COMPANIES = "companies"
private const val COL_PAYSLIPS  = "payslips"

class PayslipRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun payslipsRef(companyId: String) =
        db.collection(COL_COMPANIES)
            .document(companyId)
            .collection(COL_PAYSLIPS)

    suspend fun savePayslip(companyId: String, payslip: Payslip): String {
        val ref    = payslipsRef(companyId).document()
        val toSave = payslip.copy(id = ref.id)
        ref.set(toSave).await()
        return ref.id
    }

    suspend fun markAsPaid(companyId: String, payslipId: String) {
        payslipsRef(companyId).document(payslipId).update(
            "isPaid", true,
            "paidAt", System.currentTimeMillis()
        ).await()
    }

    fun listenToUserPayslips(companyId: String, userId: String): Flow<List<Payslip>> =
        callbackFlow {
            val l = payslipsRef(companyId)
                .whereEqualTo("userId", userId)
                .orderBy("year",  Query.Direction.DESCENDING)
                .orderBy("month", Query.Direction.DESCENDING)
                .addSnapshotListener { snap, _ ->
                    trySend(snap?.documents?.mapNotNull {
                        it.toObject(Payslip::class.java)
                    } ?: emptyList())
                }
            awaitClose { l.remove() }
        }

    fun listenToMonthPayslips(
        companyId: String, month: Int, year: Int
    ): Flow<List<Payslip>> = callbackFlow {
        val l = payslipsRef(companyId)
            .whereEqualTo("month", month)
            .whereEqualTo("year",  year)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.mapNotNull {
                    it.toObject(Payslip::class.java)
                } ?: emptyList())
            }
        awaitClose { l.remove() }
    }

    suspend fun payslipExists(
        companyId: String, userId: String, month: Int, year: Int
    ): Boolean = runCatching {
        !payslipsRef(companyId)
            .whereEqualTo("userId", userId)
            .whereEqualTo("month",  month)
            .whereEqualTo("year",   year)
            .get().await()
            .isEmpty
    }.getOrElse { false }

    suspend fun deletePayslip(companyId: String, payslipId: String) {
        payslipsRef(companyId).document(payslipId).delete().await()
    }

    suspend fun getPayslip(companyId: String, payslipId: String): Payslip? = runCatching {
        payslipsRef(companyId).document(payslipId).get().await()
            .toObject(Payslip::class.java)
    }.getOrNull()
}