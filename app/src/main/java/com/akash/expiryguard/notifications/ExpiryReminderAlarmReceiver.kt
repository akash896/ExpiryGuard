package com.akash.expiryguard.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ExpiryReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ExpiryReminderScheduler.enqueueReminderCheck(context)
        ExpiryReminderScheduler.scheduleDaily(context)
    }
}
