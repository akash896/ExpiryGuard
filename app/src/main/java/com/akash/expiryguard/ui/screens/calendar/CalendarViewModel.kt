package com.akash.expiryguard.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akash.expiryguard.data.model.ExpiryItem
import com.akash.expiryguard.data.repository.ExpiryItemRepository
import com.akash.expiryguard.util.parseIsoDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModel(
    repository: ExpiryItemRepository
) : ViewModel() {
    private val displayedMonth = MutableStateFlow(YearMonth.now())
    private val selectedDate = MutableStateFlow(LocalDate.now())

    val uiState = combine(
        repository.observeActiveItems(),
        displayedMonth,
        selectedDate
    ) { items, month, date ->
        val itemsByDate = items.mapNotNull { item ->
            parseIsoDate(item.expiryDate)?.let { expiryDate -> expiryDate to item }
        }.groupBy({ it.first }, { it.second })
        val itemsThisMonth = itemsByDate.filterKeys { it.year == month.year && it.month == month.month }

        CalendarUiState(
            displayedMonth = month,
            selectedDate = date,
            itemsByDate = itemsByDate,
            selectedDateItems = itemsByDate[date].orEmpty(),
            hasItemsThisMonth = itemsThisMonth.isNotEmpty(),
            isLoading = false
        )
    }
        .catch { error ->
            emit(
                CalendarUiState(
                    isLoading = false,
                    errorMessage = error.message ?: "Unable to load calendar items."
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CalendarUiState(isLoading = true)
        )

    fun showPreviousMonth() = updateDisplayedMonth(displayedMonth.value.minusMonths(1))

    fun showNextMonth() = updateDisplayedMonth(displayedMonth.value.plusMonths(1))

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    private fun updateDisplayedMonth(month: YearMonth) {
        displayedMonth.value = month
        val currentSelectedDate = selectedDate.value
        selectedDate.value = month.atDay(currentSelectedDate.dayOfMonth.coerceAtMost(month.lengthOfMonth()))
    }

    class Factory(
        private val repository: ExpiryItemRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CalendarViewModel(repository) as T
        }
    }
}

data class CalendarUiState(
    val displayedMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val itemsByDate: Map<LocalDate, List<ExpiryItem>> = emptyMap(),
    val selectedDateItems: List<ExpiryItem> = emptyList(),
    val hasItemsThisMonth: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

internal fun datesForCalendarMonth(month: YearMonth): List<LocalDate?> {
    val leadingEmptyCells = month.atDay(1).dayOfWeek.value - 1
    return List(leadingEmptyCells) { null } +
        (1..month.lengthOfMonth()).map(month::atDay)
}
