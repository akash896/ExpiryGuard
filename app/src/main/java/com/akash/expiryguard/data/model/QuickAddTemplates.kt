package com.akash.expiryguard.data.model

data class QuickAddTemplate(
    val templateId: String,
    val displayName: String,
    val defaultCategory: String,
    val defaultReminderDaysBefore: Int,
    val defaultQuantity: String = "",
    val defaultNotes: String = ""
)

object QuickAddTemplates {
    val all = listOf(
        QuickAddTemplate("milk", "Milk", ExpiryCategory.FOOD.displayName, 1),
        QuickAddTemplate("curd", "Curd", ExpiryCategory.FOOD.displayName, 1),
        QuickAddTemplate("bread", "Bread", ExpiryCategory.FOOD.displayName, 1),
        QuickAddTemplate("eggs", "Eggs", ExpiryCategory.FOOD.displayName, 3),
        QuickAddTemplate("medicine", "Medicine", ExpiryCategory.MEDICINE.displayName, 30),
        QuickAddTemplate("warranty", "Warranty", ExpiryCategory.WARRANTY.displayName, 30),
        QuickAddTemplate("passport", "Passport", ExpiryCategory.DOCUMENT.displayName, 30),
        QuickAddTemplate("subscription", "Subscription", ExpiryCategory.SUBSCRIPTION.displayName, 7)
    )

    fun find(templateId: String?): QuickAddTemplate? {
        return all.firstOrNull { it.templateId == templateId }
    }
}
