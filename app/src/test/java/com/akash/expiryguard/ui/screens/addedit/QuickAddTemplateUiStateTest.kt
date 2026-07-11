package com.akash.expiryguard.ui.screens.addedit

import com.akash.expiryguard.data.model.ExpiryCategory
import com.akash.expiryguard.data.model.QuickAddTemplates
import com.akash.expiryguard.util.formatIsoDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class QuickAddTemplateUiStateTest {
    @Test
    fun milkTemplatePrefillsReviewableAddState() {
        val milkTemplate = requireNotNull(QuickAddTemplates.find("milk"))

        val state = AddEditItemUiState.fromTemplate(milkTemplate)

        assertEquals("Milk", state.name)
        assertEquals(ExpiryCategory.FOOD.displayName, state.category)
        assertEquals(1, state.reminderDaysBefore)
        assertEquals(formatIsoDate(LocalDate.now()), state.purchaseDate)
        assertTrue(state.expiryDate.isBlank())
    }
}
