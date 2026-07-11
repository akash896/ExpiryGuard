package com.akash.expiryguard.ui.screens.expenses

import com.akash.expiryguard.data.model.CategoryExpenseSummary
import com.akash.expiryguard.data.model.ExpensePeriod
import com.akash.expiryguard.data.model.ExpenseSummary
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ExpenseInsightsViewModelTest {
    @Test
    fun moveReferenceDateUsesSelectedPeriod() {
        val date = LocalDate.of(2026, 7, 11)

        assertEquals(LocalDate.of(2026, 7, 10), moveReferenceDate(ExpensePeriod.DAILY, date, -1))
        assertEquals(LocalDate.of(2026, 8, 11), moveReferenceDate(ExpensePeriod.MONTHLY, date, 1))
        assertEquals(LocalDate.of(2026, 4, 11), moveReferenceDate(ExpensePeriod.QUARTERLY, date, -1))
        assertEquals(LocalDate.of(2027, 7, 11), moveReferenceDate(ExpensePeriod.ANNUALLY, date, 1))
    }

    @Test
    fun formatInsightPeriodLabelUsesReadableLabels() {
        val date = LocalDate.of(2026, 7, 11)

        assertEquals("11 Jul 2026", formatInsightPeriodLabel(ExpensePeriod.DAILY, date))
        assertEquals("July 2026", formatInsightPeriodLabel(ExpensePeriod.MONTHLY, date))
        assertEquals("Q3 2026", formatInsightPeriodLabel(ExpensePeriod.QUARTERLY, date))
        assertEquals("2026", formatInsightPeriodLabel(ExpensePeriod.ANNUALLY, date))
    }

    @Test
    fun wastePercentageAndCategorySortHandleSafeValues() {
        val summary = ExpenseSummary(
            periodLabel = "July 2026",
            totalSpent = 200.0,
            totalExpiredValue = 50.0,
            totalConsumedValue = 0.0,
            totalActiveValue = 150.0,
            itemCount = 2,
            expiredItemCount = 1,
            consumedItemCount = 0,
            categoryBreakdown = emptyMap()
        )
        val food = category("Food", spent = 80.0, expired = 30.0, itemCount = 2)
        val medicine = category("Medicine", spent = 120.0, expired = 20.0, itemCount = 1)

        assertEquals(25.0, calculateWastePercentage(summary), 0.001)
        assertEquals(
            listOf("Medicine", "Food"),
            sortCategoryBreakdown(listOf(food, medicine), ExpenseCategorySort.TOTAL_SPENT).map { it.category }
        )
        assertEquals(
            listOf("Food", "Medicine"),
            sortCategoryBreakdown(listOf(food, medicine), ExpenseCategorySort.EXPIRED_VALUE).map { it.category }
        )
    }

    private fun category(
        category: String,
        spent: Double,
        expired: Double,
        itemCount: Int
    ) = CategoryExpenseSummary(
        category = category,
        totalSpent = spent,
        totalExpiredValue = expired,
        totalConsumedValue = 0.0,
        totalActiveValue = spent - expired,
        itemCount = itemCount,
        expiredItemCount = 0,
        consumedItemCount = 0
    )
}
