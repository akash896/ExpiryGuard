package com.akash.expiryguard.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.akash.expiryguard.R
import com.akash.expiryguard.data.ExpiryItem
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object NotificationHelper {
    const val CHANNEL_ID = "expiry_reminders"
    private const val CHANNEL_NAME = "Expiry reminders"
    private const val REMINDER_NOTIFICATION_ID = 1001

    fun createReminderChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily reminders for items nearing their expiry date."
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun canPostNotifications(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun showExpiryReminder(context: Context, items: List<ExpiryItem>) {
        if (!canPostNotifications(context)) return

        val today = LocalDate.now()
        val title = if (items.size == 1) {
            "${items.first().name} needs attention"
        } else {
            "${items.size} items need attention"
        }
        val body = items
            .take(3)
            .joinToString(separator = ", ") { item ->
                val expiry = runCatching { LocalDate.parse(item.expiryDate) }.getOrNull()
                val days = expiry?.let { ChronoUnit.DAYS.between(today, it) }
                when (days) {
                    0L -> "${item.name} expires today"
                    1L -> "${item.name} expires tomorrow"
                    null -> item.name
                    else -> "${item.name} expires in $days days"
                }
            }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(REMINDER_NOTIFICATION_ID, notification)
    }
}
