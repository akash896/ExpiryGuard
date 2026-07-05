package com.akash.expiryguard.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akash.expiryguard.data.ExpiryItem
import com.akash.expiryguard.data.ExpiryItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class HomeViewModel(
    private val repository: ExpiryItemRepository
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<String?>(null)

    val uiState = combine(
        repository.observeActiveItems(),
        searchQuery,
        selectedCategory
    ) { items, query, category ->
        val filteredItems = items
            .filter { item -> item.matchesSearch(query) }
            .filter { item -> category == null || item.category == category }

        HomeUiState(
            isLoading = false,
            searchQuery = query,
            selectedCategory = category,
            totalActiveItems = filteredItems.size,
            sections = groupItems(filteredItems)
        )
    }
        .catch { error ->
            emit(
                HomeUiState(
                    isLoading = false,
                    errorMessage = error.message ?: "Unable to load expiry items."
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

    private fun ExpiryItem.matchesSearch(query: String): Boolean {
        return query.isBlank() || name.contains(query.trim(), ignoreCase = true)
    }

    private fun groupItems(items: List<ExpiryItem>): List<HomeItemSection> {
        val today = LocalDate.now()
        val datedItems = items.map { item -> item to item.daysUntilExpiry(today) }

        return listOf(
            HomeItemSection(
                title = "Expired",
                items = datedItems.filter { it.second != null && it.second!! < 0L }.map { it.first }
            ),
            HomeItemSection(
                title = "Expiring Today",
                items = datedItems.filter { it.second == 0L }.map { it.first }
            ),
            HomeItemSection(
                title = "Expiring This Week",
                items = datedItems.filter { it.second in 1L..7L }.map { it.first }
            ),
            HomeItemSection(
                title = "Expiring This Month",
                items = datedItems.filter { it.second in 8L..30L }.map { it.first }
            ),
            HomeItemSection(
                title = "Safe for Later",
                items = datedItems.filter { it.second == null || it.second!! > 30L }.map { it.first }
            )
        )
    }

    private fun ExpiryItem.daysUntilExpiry(today: LocalDate): Long? {
        val expiry = runCatching { LocalDate.parse(expiryDate) }.getOrNull() ?: return null
        return ChronoUnit.DAYS.between(today, expiry)
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
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val totalActiveItems: Int = 0,
    val sections: List<HomeItemSection> = emptyList(),
    val errorMessage: String? = null
)

data class HomeItemSection(
    val title: String,
    val items: List<ExpiryItem>
)
