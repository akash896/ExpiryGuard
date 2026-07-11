package com.akash.expiryguard.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ShoppingItemTest {
    @Test
    fun fromExpiryItemCopiesRebuyFields() {
        val expiryItem = ExpiryItem(
            id = "source-id",
            name = "Milk",
            category = ExpiryCategory.FOOD.displayName,
            quantity = "1 litre",
            price = 56.0,
            currency = "INR"
        )

        val shoppingItem = ShoppingItem.fromExpiryItem(expiryItem)

        assertEquals("Milk", shoppingItem.name)
        assertEquals(ExpiryCategory.FOOD.displayName, shoppingItem.category)
        assertEquals("1 litre", shoppingItem.quantity)
        assertEquals(56.0, shoppingItem.estimatedPrice, 0.0)
        assertEquals("INR", shoppingItem.currency)
        assertEquals("source-id", shoppingItem.sourceItemId)
        assertFalse(shoppingItem.checked)
    }
}
