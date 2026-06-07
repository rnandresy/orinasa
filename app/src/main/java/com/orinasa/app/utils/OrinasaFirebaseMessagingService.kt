package com.orinasa.app.utils

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class OrinasaFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: return
        val body  = message.notification?.body  ?: message.data["body"]  ?: return
        val type  = message.data["type"] ?: "announcement"

        val channel = when (type) {
            "leave"        -> NotificationHelper.CH_LEAVE
            "payslip"      -> NotificationHelper.CH_PAYSLIP
            "announcement" -> NotificationHelper.CH_ANNOUNCEMENT
            else           -> NotificationHelper.CH_REMINDER
        }

        NotificationHelper(applicationContext).showNotification(channel, title, body)
    }

    override fun onNewToken(token: String) {
        // Mettre à jour le token FCM dans Firestore
    }
}