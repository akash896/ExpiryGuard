package com.akash.expiryguard.ui.screens.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akash.expiryguard.data.model.ExpensePeriod
import com.akash.expiryguard.data.model.ExpenseSummary
import com.akash.expiryguard.data.model.CategoryExpenseSummary
import com.akash.expiryguard.data.repository.ExpiryItemRepository
import com.akash.expiryguard.util.calculateExpenseSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class ExpenseInsightsViewModel(
    repository: ExpiryItemRepository
) : ViewModel() {
    private val selectedPeriod = MutableStateFlow(ExpensePeriod.MONTHLY)
    private val referenceDate = MutableStateFlow(LocalDate.now())
    private val categorySort = MutableStateFlow(ExpenseCategorySort.TOTAL_SPENT)

    val uiState = combine(
        repository.observeItems(),
        selectedPeriod,
        referenceDate,
        categorySort
    ) { items, period, date, sort ->
        val summary = calculateExpenseSummary(items, period, date)
        val categories = sortCategoryBreakdown(summary.categoryBreakdown.values, sort)
        ExpenseInsightsUiState(
            selectedPeriod = period,
            referenceDate = date,
            periodLabel = formatInsightPeriodLabel(period, date),
            summary = summary,
            wastePercentage = calculateWastePercentage(summary),
            selectedCategorySort = sort,
            sortedCategories = categories,
            highestSpendingCategory = categories.maxByOrNull { it.totalSpent },
            highestExpiredValueCategory = categories.maxByOrNull { it.totalExpiredValue },
            isLoading = false
        )
    }
        .catch { error ->
            emit(
                ExpenseInsightsUiState(
                    isLoading = false,
                    errorMessage = error.message ?: "Unable to load expense insights."
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExpenseInsightsUiState(isLoading = true)
        )

    fun onPeriodSelected(period: ExpensePeriod) {
        selectedPeriod.value = period
    }

    fun showPreviousPeriod() {
        referenceDate.value = moveReferenceDate(selectedPeriod.value, referenceDate.value, -1)
    }

    fun showNextPeriod() {
        referenceDate.value = moveReferenceDate(selectedPeriod.value, referenceDate.value, 1)
    }

    fun onCategorySortSelected(sort: ExpenseCategorySort) {
        categorySort.value = sort
    }

    class Factory(
        private val repository: ExpiryItemRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExpenseInsightsViewModel(repository) as T
        }
    }
}

data class ExpenseInsightsUiState(
    val selectedPeriod: ExpensePeriod = ExpensePeriod.MONTHLY,
    val referenceDate: LocalDate = LocalDate.now(),
    val periodLabel: String = formatInsightPeriodLabel(ExpensePeriod.MONTHLY, LocalDate.now()),
    val summary: ExpenseSummary? = null,
    val wastePercentage: Double = 0.0,
    val selectedCategorySort: ExpenseCategorySort = ExpenseCategorySort.TOTAL_SPENT,
    val sortedCategories: List<CategoryExpenseSummary> = emptyList(),
    val highestSpendingCategory: CategoryExpenseSummary? = null,
    val highestExpiredValueCategory: CategoryExpenseSummary? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

enum class ExpenseCategorySort(val label: String) {
    TOTAL_SPENT("Total spent"),
    EXPIRED_VALUE("Expired value"),
    ITEM_COUNT("Item count")
}

internal fun moveReferenceDate(
    period: ExpensePeriod,
    referenceDate: LocalDate,
    direction: Long
): LocalDate {
    return when (period) {
        ExpensePeriod.DAILY -> referenceDate.plusDays(direction)
        ExpensePeriod.MONTHLY -> referenceDate.plusMonths(direction)
        ExpensePeriod.QUARTERLY -> referenceDate.plusMonths(direction * 3)
        ExpensePeriod.ANNUALLY -> referenceDate.plusYears(direction)
    }
}

internal fun formatInsightPeriodLabel(period: ExpensePeriod, referenceDate: LocalDate): String {
    return when (period) {
        ExpensePeriod.DAILY -> referenceDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US))
        ExpensePeriod.MONTHLY -> referenceDate.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US))
        ExpensePeriod.QUARTERLY -> "Q${((referenceDate.monthValue - 1) / 3) + 1} ${referenceDate.year}"
        ExpensePeriod.ANNUALLY -> referenceDate.year.toString()
    }
}

internal fun calculateWastePercentage(summary: ExpenseSummary): Double {
    val totalSpent = summary.totalSpent.takeIf { it.isFinite() && it > 0.0 } ?: return 0.0
    val expiredValue = summary.totalExpiredValue.takeIf { it.isFinite() && it > 0.0 } ?: return 0.0
    return (expiredValue / totalSpent * 100.0).coerceAtLeast(0.0)
}

internal fun sortCategoryBreakdown(
    categories: Collection<CategoryExpenseSummary>,
    sort: ExpenseCategorySort
): List<CategoryExpenseSummary> {
    val comparator = when (sort) {
        ExpenseCategorySort.TOTAL_SPENT -> compareByDescending<CategoryExpenseSummary> { it.totalSpent }
        ExpenseCategorySort.EXPIRED_VALUE -> compareByDescending<CategoryExpenseSummary> { it.totalExpiredValue }
        ExpenseCategorySort.ITEM_COUNT -> compareByDescending<CategoryExpenseSummary> { it.itemCount }
    }.thenBy { it.category }

    return categories.sortedWith(comparator)
}
