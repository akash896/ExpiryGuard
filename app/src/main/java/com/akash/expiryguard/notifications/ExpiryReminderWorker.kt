package com.akash.expiryguard.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.akash.expiryguard.data.firebase.FirebaseExpiryItemRepository
import com.akash.expiryguard.util.daysUntilExpiry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import java.time.LocalDate

class ExpiryReminderWorker(
    appContext: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        return try {
            val repository = FirebaseExpiryItemRepository()
            repository.signInAnonymouslyIfNeeded()
            val activeItems = withTimeout(FIREBASE_READ_TIMEOUT_MS) {
                repository.observeActiveItems().first()
            }
            val today = LocalDate.now()

            activeItems.forEach { item ->
                if (
                    item.archived ||
                    item.consumed ||
                    !item.notificationsEnabled ||
                    !NotificationHelper.areRemindersEnabledLocally(applicationContext, item.id)
                ) {
                    return@forEach
                }

                val daysUntilExpiry = daysUntilExpiry(item.expiryDate, today) ?: return@forEach
                val reminderDays = item.reminderDaysBefore.coerceAtLeast(0).toLong()
                val shouldNotify = daysUntilExpiry in 0L..reminderDays

                if (shouldNotify) {
                    NotificationHelper.showExpiryReminder(
                        context = applicationContext,
                        item = item,
                        daysUntilExpiry = daysUntilExpiry
                    )
                }
            }

            Result.success()
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private companion object {
        const val FIREBASE_READ_TIMEOUT_MS = 20_000L
    }
}
