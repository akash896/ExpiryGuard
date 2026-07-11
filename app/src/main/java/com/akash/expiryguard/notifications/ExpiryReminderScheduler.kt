package com.akash.expiryguard.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime

object ExpiryReminderScheduler {
    private const val LEGACY_PERIODIC_WORK_NAME = "daily_expiry_reminders"
    private const val REMINDER_CHECK_WORK_NAME = "expiry_reminder_check"
    private const val ALARM_REQUEST_CODE = 801
    private const val REMINDER_HOUR = 8

    fun scheduleDaily(context: Context) {
        val applicationContext = context.applicationContext
        val alarmManager = applicationContext.getSystemService(AlarmManager::class.java)
        val pendingIntent = alarmPendingIntent(applicationContext)
        val triggerAtMillis = System.currentTimeMillis() + delayUntilNextEightAm()

        alarmManager.cancel(pendingIntent)
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms() -> {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            else -> alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }

        WorkManager.getInstance(applicationContext).cancelUniqueWork(LEGACY_PERIODIC_WORK_NAME)
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

    internal fun delayUntilNextEightAm(now: ZonedDateTime = ZonedDateTime.now()): Long {
        var nextRun = now.toLocalDate().atTime(REMINDER_HOUR, 0).atZone(now.zone)
        if (!nextRun.isAfter(now)) {
            nextRun = nextRun.plusDays(1)
        }
        return Duration.between(now, nextRun).toMillis()
    }

    internal fun hasMissedTodayReminderTime(now: ZonedDateTime = ZonedDateTime.now()): Boolean {
        return !now.toLocalTime().isBefore(LocalTime.of(REMINDER_HOUR, 0))
    }

    private fun alarmPendingIntent(context: Context): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            Intent(context, ExpiryReminderAlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
