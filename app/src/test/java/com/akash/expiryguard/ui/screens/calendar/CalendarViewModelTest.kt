package com.akash.expiryguard.ui.screens.calendar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
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
}
