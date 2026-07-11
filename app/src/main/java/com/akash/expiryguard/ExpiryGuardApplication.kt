package com.akash.expiryguard

import android.app.Application
import com.akash.expiryguard.data.AppContainer
import com.akash.expiryguard.notifications.ExpiryReminderScheduler
import com.akash.expiryguard.notifications.NotificationHelper

class ExpiryGuardApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.createReminderChannel(this)
        ExpiryReminderScheduler.scheduleDaily(this)
    }
}
