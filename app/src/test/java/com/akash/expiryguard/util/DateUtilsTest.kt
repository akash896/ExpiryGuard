package com.akash.expiryguard.util

import com.akash.expiryguard.data.model.ExpiryStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class DateUtilsTest {
    private val today: LocalDate = LocalDate.of(2026, 7, 5)

    @Test
    fun parseIsoDate_returnsLocalDateForValidIsoDate() {
        assertEquals(LocalDate.of(2026, 7, 5), parseIsoDate("2026-07-05"))
    }

    @Test
    fun parseIsoDate_returnsNullForInvalidDate() {
        assertNull(parseIsoDate("07/05/2026"))
        assertNull(parseIsoDate(""))
    }

    @Test
    fun formatIsoDate_returnsIsoFormattedDate() {
        assertEquals("2026-07-05", formatIsoDate(today))
    }

    @Test
    fun getExpiryStatus_returnsExpiredForPastUnqualifiedDate() {
        assertEquals(ExpiryStatus.EXPIRED, getExpiryStatus("2026-07-04", today))
    }

    @Test
    fun getExpiryStatus_returnsTodayForCurrentDate() {
        assertEquals(ExpiryStatus.TODAY, getExpiryStatus("2026-07-05", today))
    }

    @Test
    fun getExpiryStatus_returnsThisWeekForOneToSevenDaysAhead() {
        assertEquals(ExpiryStatus.THIS_WEEK, getExpiryStatus("2026-07-06", today))
        assertEquals(ExpiryStatus.THIS_WEEK, getExpiryStatus("2026-07-12", today))
    }

    @Test
    fun getExpiryStatus_returnsThisMonthForEightToThirtyDaysAhead() {
        assertEquals(ExpiryStatus.THIS_MONTH, getExpiryStatus("2026-07-13", today))
        assertEquals(ExpiryStatus.THIS_MONTH, getExpiryStatus("2026-08-04", today))
    }

    @Test
    fun getExpiryStatus_returnsSafeForMoreThanThirtyDaysAheadOrInvalidDate() {
        assertEquals(ExpiryStatus.SAFE, getExpiryStatus("2026-08-05", today))
        assertEquals(ExpiryStatus.SAFE, getExpiryStatus("invalid", today))
    }

    @Test
    fun daysUntilExpiry_returnsSignedDayDifference() {
        assertEquals(-1L, daysUntilExpiry("2026-07-04", today))
        assertEquals(0L, daysUntilExpiry("2026-07-05", today))
        assertEquals(7L, daysUntilExpiry("2026-07-12", today))
    }

    @Test
    fun daysUntilExpiry_returnsNullForInvalidDate() {
        assertNull(daysUntilExpiry("not-a-date", today))
    }

    @Test
    fun dateLabels_useExpectedFormats() {
        assertEquals("2026-07", getMonthLabel(today))
        assertEquals("2026 Q3", getQuarterLabel(today))
        assertEquals("2026", getYearLabel(today))
    }
}
