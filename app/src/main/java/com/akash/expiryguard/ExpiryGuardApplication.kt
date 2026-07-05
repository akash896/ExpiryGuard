package com.akash.expiryguard

import android.app.Application
import com.akash.expiryguard.data.AppContainer
import com.akash.expiryguard.notification.ExpiryReminderScheduler
import com.akash.expiryguard.notification.NotificationHelper

class ExpiryGuardApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
        NotificationHelper.createReminderChannel(this)
        ExpiryReminderScheduler.scheduleDaily(this)
    }
}
