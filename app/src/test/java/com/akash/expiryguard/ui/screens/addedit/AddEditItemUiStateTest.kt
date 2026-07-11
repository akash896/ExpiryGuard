package com.akash.expiryguard.ui.screens.addedit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AddEditItemUiStateTest {
    private val validItem = AddEditItemUiState(
        name = "Milk",
        expiryDate = "2026-07-12"
    )

    @Test
    fun validate_allowsEmptyPrice() {
        val result = validItem.copy(price = "").validate()

        assertNull(result.priceError)
        assertEquals(true, result.canSave)
    }

    @Test
    fun validate_allowsDecimalPrice() {
        val result = validItem.copy(price = "45.75").validate()

        assertNull(result.priceError)
        assertEquals(true, result.canSave)
    }

    @Test
    fun validate_rejectsInvalidOrNonFinitePrice() {
        assertEquals("Enter a valid price.", validItem.copy(price = "abc").validate().priceError)
        assertEquals("Enter a valid price.", validItem.copy(price = "NaN").validate().priceError)
    }

    @Test
    fun validate_rejectsNegativePrice() {
        val result = validItem.copy(price = "-1.0").validate()

        assertEquals("Price cannot be negative.", result.priceError)
    }
}
