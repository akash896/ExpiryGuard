package com.akash.expiryguard.notifications

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.ZonedDateTime

class ExpiryReminderSchedulerTest {
    @Test
    fun delayUntilNextEightAm_usesTodayWhenEightAmIsStillAhead() {
        val now = ZonedDateTime.parse("2026-07-11T06:00:00+05:30[Asia/Kolkata]")

        assertEquals(Duration.ofHours(2).toMillis(), ExpiryReminderScheduler.delayUntilNextEightAm(now))
    }

    @Test
    fun delayUntilNextEightAm_usesTomorrowAtOrAfterEightAm() {
        val now = ZonedDateTime.parse("2026-07-11T08:00:00+05:30[Asia/Kolkata]")

        assertEquals(Duration.ofDays(1).toMillis(), ExpiryReminderScheduler.delayUntilNextEightAm(now))
    }
}
