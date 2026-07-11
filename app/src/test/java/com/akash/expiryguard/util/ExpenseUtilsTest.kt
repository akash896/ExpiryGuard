package com.akash.expiryguard.util

import com.akash.expiryguard.data.model.ExpensePeriod
import com.akash.expiryguard.data.model.ExpiryItem
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ExpenseUtilsTest {
    private val referenceDate: LocalDate = LocalDate.of(2026, 7, 5)

    @Test
    fun calculateTotalSpent_sumsOnlyNonNegativeFinitePrices() {
        val items = listOf(
            item(price = 100.0),
            item(price = 25.5),
            item(price = -10.0),
            item(price = Double.NaN)
        )

        assertEquals(125.5, calculateTotalSpent(items), 0.0)
    }

    @Test
    fun filterItemsByExpensePeriod_filtersDailyUsingPurchaseDate() {
        val items = listOf(
            item(id = "today", purchaseDate = "2026-07-05"),
            item(id = "yesterday", purchaseDate = "2026-07-04")
        )

        val result = filterItemsByExpensePeriod(items, ExpensePeriod.DAILY, referenceDate)

        assertEquals(listOf("today"), result.map { it.id })
    }

    @Test
    fun filterItemsByExpensePeriod_filtersMonthlyUsingPurchaseDate() {
        val items = listOf(
            item(id = "same-month", purchaseDate = "2026-07-01"),
            item(id = "different-month", purchaseDate = "2026-08-01"),
            item(id = "different-year", purchaseDate = "2025-07-01")
        )

        val result = filterItemsByExpensePeriod(items, ExpensePeriod.MONTHLY, referenceDate)

        assertEquals(listOf("same-month"), result.map { it.id })
    }

    @Test
    fun filterItemsByExpensePeriod_filtersQuarterlyUsingPurchaseDate() {
        val items = listOf(
            item(id = "july", purchaseDate = "2026-07-01"),
            item(id = "september", purchaseDate = "2026-09-30"),
            item(id = "october", purchaseDate = "2026-10-01"),
            item(id = "last-year", purchaseDate = "2025-07-01")
        )

        val result = filterItemsByExpensePeriod(items, ExpensePeriod.QUARTERLY, referenceDate)

        assertEquals(listOf("july", "september"), result.map { it.id })
    }

    @Test
    fun filterItemsByExpensePeriod_filtersAnnuallyUsingPurchaseDate() {
        val items = listOf(
            item(id = "this-year", purchaseDate = "2026-01-01"),
            item(id = "next-year", purchaseDate = "2027-01-01"),
            item(id = "last-year", purchaseDate = "2025-12-31")
        )

        val result = filterItemsByExpensePeriod(items, ExpensePeriod.ANNUALLY, referenceDate)

        assertEquals(listOf("this-year"), result.map { it.id })
    }

    @Test
    fun filterItemsByExpensePeriod_fallsBackToExpiryDateWhenPurchaseDateIsEmptyOrInvalid() {
        val items = listOf(
            item(id = "empty-purchase", purchaseDate = "", expiryDate = "2026-07-05"),
            item(id = "invalid-purchase", purchaseDate = "bad-date", expiryDate = "2026-07-05"),
            item(id = "invalid-both", purchaseDate = "bad-date", expiryDate = "also-bad")
        )

        val result = filterItemsByExpensePeriod(items, ExpensePeriod.DAILY, referenceDate)

        assertEquals(listOf("empty-purchase", "invalid-purchase"), result.map { it.id })
    }

    @Test
    fun groupExpensesByCategory_calculatesCategoryWiseBreakdown() {
        val items = listOf(
            item(category = "Food", price = 100.0, expiryDate = "2026-07-04"),
            item(category = "Food", price = 50.0, expiryDate = "2026-07-10", consumed = true),
            item(category = "Medicine", price = 200.0, expiryDate = "2026-08-01")
        )

        val result = groupExpensesByCategory(items, referenceDate)

        val food = result.getValue("Food")
        assertEquals(150.0, food.totalSpent, 0.0)
        assertEquals(100.0, food.totalExpiredValue, 0.0)
        assertEquals(50.0, food.totalConsumedValue, 0.0)
        assertEquals(2, food.itemCount)
        assertEquals(1, food.expiredItemCount)
        assertEquals(1, food.consumedItemCount)

        val medicine = result.getValue("Medicine")
        assertEquals(200.0, medicine.totalActiveValue, 0.0)
    }

    @Test
    fun calculateTotalExpiredValue_includesOnlyExpiredUnconsumedItems() {
        val items = listOf(
            item(price = 100.0, expiryDate = "2026-07-04"),
            item(price = 50.0, expiryDate = "2026-07-04", consumed = true),
            item(price = 25.0, expiryDate = "2026-07-05"),
            item(price = 10.0, expiryDate = "invalid")
        )

        assertEquals(100.0, calculateTotalExpiredValue(items, referenceDate), 0.0)
    }

    @Test
    fun calculateTotalConsumedValue_includesOnlyConsumedItems() {
        val items = listOf(
            item(price = 100.0, consumed = true),
            item(price = 50.0, consumed = false),
            item(price = -20.0, consumed = true),
            item(price = Double.NaN, consumed = true)
        )

        assertEquals(100.0, calculateTotalConsumedValue(items), 0.0)
    }

    @Test
    fun calculateTotalActiveValue_includesOnlyUnconsumedNonExpiredItems() {
        val items = listOf(
            item(price = 100.0, expiryDate = "2026-07-10"),
            item(price = 50.0, expiryDate = "2026-07-04"),
            item(price = 25.0, expiryDate = "2026-07-10", consumed = true),
            item(price = -5.0, expiryDate = "2026-07-10")
        )

        assertEquals(100.0, calculateTotalActiveValue(items, referenceDate), 0.0)
    }

    @Test
    fun calculateExpenseSummary_usesFilteredItemsAndPeriodLabel() {
        val items = listOf(
            item(category = "Food", price = 100.0, purchaseDate = "2026-07-05", expiryDate = "2026-07-04"),
            item(category = "Food", price = 50.0, purchaseDate = "2026-07-05", expiryDate = "2026-07-10", consumed = true),
            item(category = "Other", price = 999.0, purchaseDate = "2026-07-04", expiryDate = "2026-07-10")
        )

        val summary = calculateExpenseSummary(items, ExpensePeriod.DAILY, referenceDate)

        assertEquals("2026-07-05", summary.periodLabel)
        assertEquals(150.0, summary.totalSpent, 0.0)
        assertEquals(100.0, summary.totalExpiredValue, 0.0)
        assertEquals(50.0, summary.totalConsumedValue, 0.0)
        assertEquals(2, summary.itemCount)
        assertEquals(1, summary.expiredItemCount)
        assertEquals(1, summary.consumedItemCount)
        assertEquals(setOf("Food"), summary.categoryBreakdown.keys)
    }

    private fun item(
        id: String = "",
        category: String = "Other",
        expiryDate: String = "2026-08-01",
        purchaseDate: String = "2026-07-05",
        price: Double = 0.0,
        consumed: Boolean = false
    ): ExpiryItem {
        return ExpiryItem(
            id = id,
            category = category,
            expiryDate = expiryDate,
            purchaseDate = purchaseDate,
            price = price,
            consumed = consumed
        )
    }
}
