package com.akash.expiryguard.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class QuickAddTemplatesTest {
    @Test
    fun milkTemplateHasExpectedDefaults() {
        val template = QuickAddTemplates.find("milk")

        assertNotNull(template)
        assertEquals("Milk", template?.displayName)
        assertEquals(ExpiryCategory.FOOD.displayName, template?.defaultCategory)
        assertEquals(1, template?.defaultReminderDaysBefore)
    }

    @Test
    fun allTemplateIdsAreUnique() {
        val templateIds = QuickAddTemplates.all.map { it.templateId }

        assertEquals(templateIds.size, templateIds.toSet().size)
    }
}
