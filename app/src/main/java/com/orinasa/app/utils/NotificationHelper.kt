package com.orinasa.app.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.orinasa.app.MainActivity

class NotificationHelper(private val context: Context) {

    companion object {
        const val CH_LEAVE       = "orinasa_leave"
        const val CH_PAYSLIP     = "orinasa_payslip"
        const val CH_ANNOUNCEMENT = "orinasa_announcement"
        const val CH_REMINDER    = "orinasa_reminder"
        private var idCounter    = 1000
    }

    fun createChannels() {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        listOf(
            Triple(CH_LEAVE,        "Congés",          NotificationManager.IMPORTANCE_HIGH),
            Triple(CH_PAYSLIP,      "Fiches de paie",  NotificationManager.IMPORTANCE_DEFAULT),
            Triple(CH_ANNOUNCEMENT, "Annonces",        NotificationManager.IMPORTANCE_HIGH),
            Triple(CH_REMINDER,     "Rappels",         NotificationManager.IMPORTANCE_DEFAULT)
        ).forEach { (id, name, importance) ->
            nm.createNotificationChannel(
                NotificationChannel(id, name, importance)
            )
        }
    }

    private fun hasPermission() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        else true

    private fun tapIntent() = PendingIntent.getActivity(
        context, 0,
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    fun showNotification(channel: String, title: String, message: String) {
        if (!hasPermission()) return
        runCatching {
            NotificationManagerCompat.from(context).notify(idCounter++,
                NotificationCompat.Builder(context, channel)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                    .setAutoCancel(true)
                    .setPriority(
                        if (channel == CH_LEAVE || channel == CH_ANNOUNCEMENT)
                            NotificationCompat.PRIORITY_HIGH
                        else NotificationCompat.PRIORITY_DEFAULT
                    )
                    .setContentIntent(tapIntent())
                    .build()
            )
        }
    }
}