package com.akash.expiryguard.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akash.expiryguard.data.model.ExpensePeriod
import com.akash.expiryguard.data.model.ExpiryItem
import com.akash.expiryguard.data.repository.ExpiryItemRepository
import com.akash.expiryguard.util.calculateExpenseSummary
import com.akash.expiryguard.util.calculateTotalActiveValue
import com.akash.expiryguard.util.calculateTotalConsumedValue
import com.akash.expiryguard.util.calculateTotalExpiredValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class HomeViewModel(
    repository: ExpiryItemRepository
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<String?>(null)

    val uiState = combine(
        repository.observeActiveItems(),
        searchQuery,
        selectedCategory
    ) { items, query, category ->
        val trimmedQuery = query.trim()
        val filteredItems = items
            .filter { item -> trimmedQuery.isBlank() || item.name.contains(trimmedQuery, ignoreCase = true) }
            .filter { item -> category == null || item.category == category }

        val today = LocalDate.now()
        val monthSummary = calculateExpenseSummary(items, ExpensePeriod.MONTHLY, today)

        HomeUiState(
            items = filteredItems,
            searchQuery = query,
            selectedCategory = category,
            totalActiveValue = calculateTotalActiveValue(items, today),
            totalExpiredValue = calculateTotalExpiredValue(items, today),
            totalConsumedValue = calculateTotalConsumedValue(items),
            currentMonthSpending = monthSummary.totalSpent,
            isLoading = false
        )
    }
        .catch { error ->
            emit(
                HomeUiState(
                    isLoading = false,
                    errorMessage = error.message ?: "Unable to load items."
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(isLoading = true)
        )

    fun onSearchQueryChange(value: String) {
        searchQuery.value = value
    }

    fun onCategorySelected(category: String?) {
        selectedCategory.value = category
    }

    class Factory(
        private val repository: ExpiryItemRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}

data class HomeUiState(
    val items: List<ExpiryItem> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val totalActiveValue: Double = 0.0,
    val totalExpiredValue: Double = 0.0,
    val totalConsumedValue: Double = 0.0,
    val currentMonthSpending: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
