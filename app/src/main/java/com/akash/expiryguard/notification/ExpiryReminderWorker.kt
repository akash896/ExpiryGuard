package com.akash.expiryguard.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.akash.expiryguard.data.ExpiryItem
import com.akash.expiryguard.data.firebase.FirebaseExpiryItemRepository
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ExpiryReminderWorker(
    appContext: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        return try {
            val repository = FirebaseExpiryItemRepository()
            repository.ensureSignedIn()
            val dueItems = repository.getActiveItems().filter { it.isWithinReminderWindow() }

            if (dueItems.isNotEmpty()) {
                NotificationHelper.showExpiryReminder(applicationContext, dueItems)
            }

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private fun ExpiryItem.isWithinReminderWindow(today: LocalDate = LocalDate.now()): Boolean {
        val expiry = runCatching { LocalDate.parse(expiryDate) }.getOrNull() ?: return false
        val daysUntilExpiry = ChronoUnit.DAYS.between(today, expiry)
        return daysUntilExpiry in 0..reminderDaysBefore.toLong()
    }
}
