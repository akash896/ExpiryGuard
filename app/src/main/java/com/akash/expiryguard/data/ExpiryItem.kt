package com.akash.expiryguard.data

data class ExpiryItem(
    val id: String = "",
    val name: String = "",
    val category: String = ItemCategory.OTHER.displayName,
    val expiryDate: String = "",
    val quantity: String = "",
    val notes: String = "",
    val reminderDaysBefore: Int = 3,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val archived: Boolean = false
)
