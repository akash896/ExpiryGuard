package com.akash.expiryguard.data.model

data class ShoppingItem(
    val id: String = "",
    val name: String = "",
    val category: String = ExpiryCategory.OTHER.displayName,
    val quantity: String = "",
    val estimatedPrice: Double = 0.0,
    val currency: String = "INR",
    val sourceItemId: String = "",
    val checked: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    companion object {
        fun fromExpiryItem(item: ExpiryItem): ShoppingItem {
            return ShoppingItem(
                name = item.name,
                category = item.category,
                quantity = item.quantity,
                estimatedPrice = item.price,
                currency = item.currency,
                sourceItemId = item.id
            )
        }
    }
}
