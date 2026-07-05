package com.akash.expiryguard.util

import com.akash.expiryguard.data.model.ExpiryStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

private val IsoDateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

fun parseIsoDate(date: String): LocalDate? {
    if (date.isBlank()) return null
    return try {
        LocalDate.parse(date, IsoDateFormatter)
    } catch (_: DateTimeParseException) {
        null
    }
}

fun formatIsoDate(date: LocalDate): String {
    return date.format(IsoDateFormatter)
}

fun getExpiryStatus(
    expiryDate: String,
    today: LocalDate = LocalDate.now()
): ExpiryStatus {
    val daysUntilExpiry = daysUntilExpiry(expiryDate, today) ?: return ExpiryStatus.SAFE
    return when {
        daysUntilExpiry < 0L -> ExpiryStatus.EXPIRED
        daysUntilExpiry == 0L -> ExpiryStatus.TODAY
        daysUntilExpiry <= 7L -> ExpiryStatus.THIS_WEEK
        daysUntilExpiry <= 30L -> ExpiryStatus.THIS_MONTH
        else -> ExpiryStatus.SAFE
    }
}

fun daysUntilExpiry(
    expiryDate: String,
    today: LocalDate = LocalDate.now()
): Long? {
    val expiry = parseIsoDate(expiryDate) ?: return null
    return ChronoUnit.DAYS.between(today, expiry)
}

fun getQuarterLabel(date: LocalDate): String {
    val quarter = ((date.monthValue - 1) / 3) + 1
    return "${date.year} Q$quarter"
}

fun getMonthLabel(date: LocalDate): String {
    return "%04d-%02d".format(date.year, date.monthValue)
}

fun getYearLabel(date: LocalDate): String {
    return date.year.toString()
}
