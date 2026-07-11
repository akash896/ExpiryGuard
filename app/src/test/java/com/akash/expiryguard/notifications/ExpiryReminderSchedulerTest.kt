package com.akash.expiryguard.notifications

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.ZonedDateTime

class ExpiryReminderSchedulerTest {
    @Test
    fun delayUntilNextReminderTime_usesTodayWhenSelectedTimeIsStillAhead() {
        val now = ZonedDateTime.parse("2026-07-11T06:00:00+05:30[Asia/Kolkata]")

        assertEquals(
            Duration.ofHours(3).toMillis(),
            ExpiryReminderScheduler.delayUntilNextReminderTime(9, 0, now)
        )
    }

    @Test
    fun delayUntilNextReminderTime_usesTomorrowAtOrAfterSelectedTime() {
        val now = ZonedDateTime.parse("2026-07-11T09:30:00+05:30[Asia/Kolkata]")

        assertEquals(
            Duration.ofHours(23).plusMinutes(30).toMillis(),
            ExpiryReminderScheduler.delayUntilNextReminderTime(9, 0, now)
        )
    }
}
