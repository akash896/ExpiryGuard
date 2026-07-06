package com.akash.expiryguard.ui.screens.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akash.expiryguard.data.model.ExpensePeriod
import com.akash.expiryguard.data.model.ExpenseSummary
import com.akash.expiryguard.data.repository.ExpiryItemRepository
import com.akash.expiryguard.util.calculateExpenseSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class ExpenseInsightsViewModel(
    repository: ExpiryItemRepository
) : ViewModel() {
    private val selectedPeriod = MutableStateFlow(ExpensePeriod.MONTHLY)
    private val referenceDate = MutableStateFlow(LocalDate.now())

    val uiState = combine(
        repository.observeItems(),
        selectedPeriod,
        referenceDate
    ) { items, period, date ->
        ExpenseInsightsUiState(
            selectedPeriod = period,
            referenceDate = date,
            summary = calculateExpenseSummary(items, period, date),
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
    val summary: ExpenseSummary? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
