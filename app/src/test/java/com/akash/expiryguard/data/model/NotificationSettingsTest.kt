package com.akash.expiryguard.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationSettingsTest {
    @Test
    fun categoryDefaultsMatchReminderPolicy() {
        assertEquals(1, CategoryReminderDefaults.reminderDaysFor(ExpiryCategory.FOOD.displayName))
        assertEquals(30, CategoryReminderDefaults.reminderDaysFor(ExpiryCategory.MEDICINE.displayName))
        assertEquals(30, CategoryReminderDefaults.reminderDaysFor(ExpiryCategory.DOCUMENT.displayName))
        assertEquals(30, CategoryReminderDefaults.reminderDaysFor(ExpiryCategory.WARRANTY.displayName))
        assertEquals(7, CategoryReminderDefaults.reminderDaysFor(ExpiryCategory.COSMETIC.displayName))
        assertEquals(7, CategoryReminderDefaults.reminderDaysFor(ExpiryCategory.SUBSCRIPTION.displayName))
        assertEquals(7, CategoryReminderDefaults.reminderDaysFor(ExpiryCategory.OTHER.displayName))
    }

    @Test
    fun notificationSettingsFallBackToOtherForUnknownCategory() {
        assertEquals(7, NotificationSettings().reminderDaysFor("Unknown"))
    }
}
