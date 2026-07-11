package com.akash.expiryguard.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.akash.expiryguard.data.firebase.FirebaseExpiryItemRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != NotificationHelper.ACTION_STOP_REMINDERS) return

        val itemId = intent.getStringExtra(NotificationHelper.EXTRA_ITEM_ID).orEmpty()
        if (itemId.isBlank()) return

        val notificationId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, -1)
        NotificationHelper.setRemindersEnabledLocally(context, itemId, false)
        val pendingResult = goAsync()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                FirebaseExpiryItemRepository().setNotificationsEnabled(itemId, false)
                if (notificationId >= 0) {
                    NotificationHelper.cancelReminder(context, notificationId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
