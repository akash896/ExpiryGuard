package com.akash.expiryguard.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.akash.expiryguard.data.local.AppPreferences
import com.akash.expiryguard.data.model.NotificationSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object ExpiryReminderScheduler {
    private const val DAILY_REMINDER_WORK_NAME = "daily_expiry_reminders"
    private const val REMINDER_CHECK_WORK_NAME = "expiry_reminder_check"

    fun scheduleDaily(context: Context) {
        val applicationContext = context.applicationContext
        CoroutineScope(Dispatchers.Default).launch {
            val settings = AppPreferences(applicationContext).notificationSettings.first()
            scheduleDaily(applicationContext, settings)
        }
    }

    fun scheduleDaily(context: Context, settings: NotificationSettings) {
        val applicationContext = context.applicationContext
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val delay = delayUntilNextReminderTime(
            hour = settings.reminderCheckHour,
            minute = settings.reminderCheckMinute
        )
        val request = PeriodicWorkRequestBuilder<ExpiryReminderWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            DAILY_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun enqueueReminderCheck(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<ExpiryReminderWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            REMINDER_CHECK_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    internal fun delayUntilNextReminderTime(
        hour: Int,
        minute: Int,
        now: ZonedDateTime = ZonedDateTime.now()
    ): Long {
        var nextRun = now.toLocalDate()
            .atTime(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
            .atZone(now.zone)
        if (!nextRun.isAfter(now)) {
            nextRun = nextRun.plusDays(1)
        }
        return Duration.between(now, nextRun).toMillis()
    }
}
