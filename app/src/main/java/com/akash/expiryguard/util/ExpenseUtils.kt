package com.akash.expiryguard.util

import com.akash.expiryguard.data.model.CategoryExpenseSummary
import com.akash.expiryguard.data.model.ExpensePeriod
import com.akash.expiryguard.data.model.ExpenseSummary
import com.akash.expiryguard.data.model.ExpiryItem
import java.time.LocalDate

fun calculateTotalSpent(items: List<ExpiryItem>): Double {
    return items.sumOf { it.safePrice() }
}

fun calculateTotalExpiredValue(
    items: List<ExpiryItem>,
    today: LocalDate = LocalDate.now()
): Double {
    return items
        .filter { it.isExpired(today) && !it.consumed }
        .sumOf { it.safePrice() }
}

fun calculateTotalConsumedValue(items: List<ExpiryItem>): Double {
    return items
        .filter { it.consumed }
        .sumOf { it.safePrice() }
}

fun calculateTotalActiveValue(
    items: List<ExpiryItem>,
    today: LocalDate = LocalDate.now()
): Double {
    return items
        .filter { !it.consumed && !it.isExpired(today) }
        .sumOf { it.safePrice() }
}

fun groupExpensesByCategory(
    items: List<ExpiryItem>,
    today: LocalDate = LocalDate.now()
): Map<String, CategoryExpenseSummary> {
    return items
        .groupBy { it.category.ifBlank { "Other" } }
        .mapValues { (category, categoryItems) ->
            CategoryExpenseSummary(
                category = category,
                totalSpent = calculateTotalSpent(categoryItems),
                totalExpiredValue = calculateTotalExpiredValue(categoryItems, today),
                totalConsumedValue = calculateTotalConsumedValue(categoryItems),
                totalActiveValue = calculateTotalActiveValue(categoryItems, today),
                itemCount = categoryItems.size,
                expiredItemCount = categoryItems.count { it.isExpired(today) && !it.consumed },
                consumedItemCount = categoryItems.count { it.consumed }
            )
        }
}

fun filterItemsByExpensePeriod(
    items: List<ExpiryItem>,
    period: ExpensePeriod,
    referenceDate: LocalDate = LocalDate.now()
): List<ExpiryItem> {
    return items.filter { item ->
        val itemDate = item.expenseDate() ?: return@filter false
        when (period) {
            ExpensePeriod.DAILY -> itemDate == referenceDate
            ExpensePeriod.MONTHLY -> itemDate.year == referenceDate.year &&
                itemDate.month == referenceDate.month
            ExpensePeriod.QUARTERLY -> itemDate.year == referenceDate.year &&
                itemDate.quarter() == referenceDate.quarter()
            ExpensePeriod.ANNUALLY -> itemDate.year == referenceDate.year
        }
    }
}

fun calculateExpenseSummary(
    items: List<ExpiryItem>,
    period: ExpensePeriod,
    referenceDate: LocalDate = LocalDate.now()
): ExpenseSummary {
    val filteredItems = filterItemsByExpensePeriod(items, period, referenceDate)
    return ExpenseSummary(
        periodLabel = period.label(referenceDate),
        totalSpent = calculateTotalSpent(filteredItems),
        totalExpiredValue = calculateTotalExpiredValue(filteredItems, referenceDate),
        totalConsumedValue = calculateTotalConsumedValue(filteredItems),
        totalActiveValue = calculateTotalActiveValue(filteredItems, referenceDate),
        itemCount = filteredItems.size,
        expiredItemCount = filteredItems.count { it.isExpired(referenceDate) && !it.consumed },
        consumedItemCount = filteredItems.count { it.consumed },
        categoryBreakdown = groupExpensesByCategory(filteredItems, referenceDate)
    )
}

private fun ExpiryItem.safePrice(): Double {
    return if (price.isFinite() && price > 0.0) price else 0.0
}

private fun ExpiryItem.isExpired(today: LocalDate): Boolean {
    val expiry = parseIsoDate(expiryDate) ?: return false
    return expiry.isBefore(today)
}

private fun ExpiryItem.expenseDate(): LocalDate? {
    return parseIsoDate(purchaseDate) ?: parseIsoDate(expiryDate)
}

private fun LocalDate.quarter(): Int {
    return ((monthValue - 1) / 3) + 1
}

private fun ExpensePeriod.label(referenceDate: LocalDate): String {
    return when (this) {
        ExpensePeriod.DAILY -> formatIsoDate(referenceDate)
        ExpensePeriod.MONTHLY -> getMonthLabel(referenceDate)
        ExpensePeriod.QUARTERLY -> getQuarterLabel(referenceDate)
        ExpensePeriod.ANNUALLY -> getYearLabel(referenceDate)
    }
}
