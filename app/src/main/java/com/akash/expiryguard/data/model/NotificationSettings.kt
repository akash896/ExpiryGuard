package com.akash.expiryguard.data.model

object CategoryReminderDefaults {
    val categories = listOf(
        ExpiryCategory.FOOD.displayName,
        ExpiryCategory.MEDICINE.displayName,
        ExpiryCategory.DOCUMENT.displayName,
        ExpiryCategory.WARRANTY.displayName,
        ExpiryCategory.COSMETIC.displayName,
        ExpiryCategory.SUBSCRIPTION.displayName,
        ExpiryCategory.OTHER.displayName
    )

    val defaults = linkedMapOf(
        ExpiryCategory.FOOD.displayName to 1,
        ExpiryCategory.MEDICINE.displayName to 30,
        ExpiryCategory.DOCUMENT.displayName to 30,
        ExpiryCategory.WARRANTY.displayName to 30,
        ExpiryCategory.COSMETIC.displayName to 7,
        ExpiryCategory.SUBSCRIPTION.displayName to 7,
        ExpiryCategory.OTHER.displayName to 7
    )

    fun reminderDaysFor(category: String): Int {
        return defaults[category] ?: defaults.getValue(ExpiryCategory.OTHER.displayName)
    }
}

data class NotificationSettings(
    val reminderCheckHour: Int = DEFAULT_REMINDER_CHECK_HOUR,
    val reminderCheckMinute: Int = DEFAULT_REMINDER_CHECK_MINUTE,
    val categoryReminderDays: Map<String, Int> = CategoryReminderDefaults.defaults
) {
    fun reminderDaysFor(category: String): Int {
        return categoryReminderDays[category]?.coerceAtLeast(0)
            ?: CategoryReminderDefaults.reminderDaysFor(category)
    }

    companion object {
        const val DEFAULT_REMINDER_CHECK_HOUR = 9
        const val DEFAULT_REMINDER_CHECK_MINUTE = 0
    }
}
