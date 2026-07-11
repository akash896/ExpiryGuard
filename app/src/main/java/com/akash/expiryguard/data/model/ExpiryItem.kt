package com.akash.expiryguard.data.model

data class ExpiryItem(
    val id: String = "",
    val name: String = "",
    val category: String = ExpiryCategory.OTHER.displayName,
    val expiryDate: String = "",
    val purchaseDate: String = "",
    val quantity: String = "",
    val price: Double = 0.0,
    val currency: String = "INR",
    val notes: String = "",
    val reminderDaysBefore: Int = 1,
    val notificationsEnabled: Boolean = true,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val archived: Boolean = false,
    val consumed: Boolean = false,
    val consumedAt: String = ""
)
