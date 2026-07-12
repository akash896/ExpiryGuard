package com.akash.expiryguard.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.akash.expiryguard.MainActivity
import com.akash.expiryguard.R
import com.akash.expiryguard.data.model.ExpiryItem
import java.util.Locale

object NotificationHelper {
    const val CHANNEL_ID = "expiry_reminders"
    const val ACTION_STOP_REMINDERS = "com.akash.expiryguard.action.STOP_REMINDERS"
    const val EXTRA_ITEM_ID = "item_id"
    const val EXTRA_NOTIFICATION_ID = "notification_id"
    private const val NOTIFICATION_PREFERENCES = "expiry_reminder_notifications"
    private const val STOPPED_REMINDERS_PREFERENCES = "stopped_expiry_reminders"
    private const val TEST_NOTIFICATION_ID = 802

    fun createReminderChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Expiry reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun canPostNotifications(context: Context): Boolean {
        val runtimePermissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        return runtimePermissionGranted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun canRequestNotificationPermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
    }

    fun showExpiryReminder(
        context: Context,
        item: ExpiryItem,
        daysUntilExpiry: Long
    ): Boolean {
        if (!canPostNotifications(context) || !areRemindersEnabledLocally(context, item.id)) {
            return false
        }

        val notificationKey = "${item.id}|${item.expiryDate}|${java.time.LocalDate.now()}"
        val reminderNotificationId = notificationId(notificationKey)
        val preferences = context.getSharedPreferences(
            NOTIFICATION_PREFERENCES,
            Context.MODE_PRIVATE
        )
        if (preferences.getBoolean(notificationKey, false)) return false

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_expiry_reminder)
            .setContentTitle(notificationTitle(item.name, daysUntilExpiry))
            .setContentText(notificationBody(item))
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationBody(item)))
            .setContentIntent(createContentIntent(context, reminderNotificationId))
            .addAction(
                R.drawable.ic_stat_expiry_reminder,
                "Stop",
                createStopIntent(context, item.id, reminderNotificationId)
            )
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(reminderNotificationId, notification)
            preferences.edit().putBoolean(notificationKey, true).apply()
            true
        } catch (_: SecurityException) {
            false
        }
    }

    fun showTestNotification(context: Context): Boolean {
        if (!canPostNotifications(context)) return false

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_expiry_reminder)
            .setContentTitle("ExpiryGuard test reminder")
            .setContentText("Notifications are ready for your expiry reminders.")
            .setContentIntent(createContentIntent(context, TEST_NOTIFICATION_ID))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(TEST_NOTIFICATION_ID, notification)
            true
        } catch (_: SecurityException) {
            false
        }
    }

    private fun notificationTitle(name: String, daysUntilExpiry: Long): String {
        val itemName = name.ifBlank { "An item" }
        return when (daysUntilExpiry) {
            0L -> "$itemName expires today"
            1L -> "$itemName expires tomorrow"
            else -> "$itemName expires in $daysUntilExpiry days"
        }
    }

    private fun notificationBody(item: ExpiryItem): String {
        val parts = mutableListOf(
            item.category.ifBlank { "Other" },
            "Expires ${item.expiryDate}"
        )
        item.price.takeIf { it.isFinite() && it > 0.0 }?.let { price ->
            parts += "Value ${formatMoney(price, item.currency)}"
        }
        return parts.joinToString(" • ")
    }

    private fun formatMoney(price: Double, currency: String): String {
        val amount = String.format(Locale.US, "%.2f", price)
        return if (currency.equals("INR", ignoreCase = true) || currency.isBlank()) {
            "₹$amount"
        } else {
            "${currency.uppercase(Locale.US)} $amount"
        }
    }

    private fun createContentIntent(context: Context, requestCode: Int) =
        android.app.PendingIntent.getActivity(
            context,
            requestCode,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

    private fun createStopIntent(context: Context, itemId: String, reminderNotificationId: Int) =
        android.app.PendingIntent.getBroadcast(
            context,
            reminderNotificationId,
            Intent(context, NotificationActionReceiver::class.java).apply {
                action = ACTION_STOP_REMINDERS
                putExtra(EXTRA_ITEM_ID, itemId)
                putExtra(EXTRA_NOTIFICATION_ID, reminderNotificationId)
            },
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

    fun cancelReminder(context: Context, reminderNotificationId: Int) {
        NotificationManagerCompat.from(context).cancel(reminderNotificationId)
    }

    fun setRemindersEnabledLocally(context: Context, itemId: String, enabled: Boolean) {
        if (itemId.isBlank()) return
        context.getSharedPreferences(STOPPED_REMINDERS_PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(itemId, !enabled)
            .apply()
    }

    fun areRemindersEnabledLocally(context: Context, itemId: String): Boolean {
        if (itemId.isBlank()) return false
        return !context.getSharedPreferences(STOPPED_REMINDERS_PREFERENCES, Context.MODE_PRIVATE)
            .getBoolean(itemId, false)
    }

    private fun notificationId(notificationKey: String): Int = notificationKey.hashCode() and Int.MAX_VALUE
}
