package com.akash.expiryguard.ui.screens.calendar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import com.akash.expiryguard.data.model.ExpiryItem
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModelTest {
    @Test
    fun datesForCalendarMonthStartsOnMondayAndContainsEveryDate() {
        val july2026 = YearMonth.of(2026, 7)
        val dates = datesForCalendarMonth(july2026)

        assertEquals(2, dates.takeWhile { it == null }.size)
        assertEquals(LocalDate.of(2026, 7, 1), dates[2])
        assertEquals(LocalDate.of(2026, 7, 31), dates.last())
    }

    @Test
    fun datesForCalendarMonthHasNoLeadingCellsWhenMonthStartsOnMonday() {
        val june2026 = YearMonth.of(2026, 6)
        val dates = datesForCalendarMonth(june2026)

        assertEquals(LocalDate.of(2026, 6, 1), dates.first())
        assertNull(dates.firstOrNull { it == null })
    }

    @Test
    fun groupItemsByExpiryDateKeepsEveryItemOnTheSameDate() {
        val items = listOf(
            ExpiryItem(id = "milk", expiryDate = "2026-07-12"),
            ExpiryItem(id = "curd", expiryDate = "2026-07-12"),
            ExpiryItem(id = "bread", expiryDate = "2026-07-15"),
            ExpiryItem(id = "invalid", expiryDate = "not-a-date")
        )

        val groupedItems = groupItemsByExpiryDate(items)

        assertEquals(2, groupedItems[LocalDate.of(2026, 7, 12)]?.size)
        assertEquals(1, groupedItems[LocalDate.of(2026, 7, 15)]?.size)
        assertEquals(2, groupedItems.size)
    }
}
