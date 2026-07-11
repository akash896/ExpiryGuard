package com.akash.expiryguard.ui.screens.addedit

import com.akash.expiryguard.data.model.ExpiryCategory
import com.akash.expiryguard.data.model.NotificationSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryReminderSelectionTest {
    private val settings = NotificationSettings(
        categoryReminderDays = mapOf(
            ExpiryCategory.FOOD.displayName to 3,
            ExpiryCategory.OTHER.displayName to 7
        )
    )

    @Test
    fun categoryChangeUsesConfiguredDefaultUntilReminderIsManuallyChanged() {
        val reminderDays = reminderDaysForCategoryChange(
            currentReminderDays = 7,
            category = ExpiryCategory.FOOD.displayName,
            reminderDaysManuallyChanged = false,
            notificationSettings = settings
        )

        assertEquals(3, reminderDays)
    }

    @Test
    fun categoryChangeKeepsManualReminderValue() {
        val reminderDays = reminderDaysForCategoryChange(
            currentReminderDays = 15,
            category = ExpiryCategory.FOOD.displayName,
            reminderDaysManuallyChanged = true,
            notificationSettings = settings
        )

        assertEquals(15, reminderDays)
    }
}
