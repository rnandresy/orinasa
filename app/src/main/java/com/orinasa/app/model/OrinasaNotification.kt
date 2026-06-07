package com.orinasa.app.model

data class OrinasaNotification(
    val id: String = "",
    val targetUserId: String = "",
    val companyId: String = "",
    val type: String = "",
    // Types : "leave_request" | "leave_approved" | "leave_rejected"
    //         | "payslip_ready" | "announcement" | "clock_reminder"
    val title: String = "",
    val message: String = "",
    val referenceId: String = "",        // ID de l'objet lié
    val isRead: Boolean = false,
    val timestamp: Long = 0L
)