package com.akash.expiryguard.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val supportedAction = intent.action in setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED
        )
        if (!supportedAction) return

        ExpiryReminderScheduler.scheduleDaily(context)
        if (ExpiryReminderScheduler.hasMissedTodayReminderTime()) {
            ExpiryReminderScheduler.enqueueReminderCheck(context)
        }
    }
}
