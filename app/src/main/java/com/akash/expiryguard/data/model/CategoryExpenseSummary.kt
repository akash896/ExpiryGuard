package com.akash.expiryguard.data.model

data class CategoryExpenseSummary(
    val category: String,
    val totalSpent: Double,
    val totalExpiredValue: Double,
    val totalConsumedValue: Double,
    val totalActiveValue: Double,
    val itemCount: Int,
    val expiredItemCount: Int,
    val consumedItemCount: Int
)
