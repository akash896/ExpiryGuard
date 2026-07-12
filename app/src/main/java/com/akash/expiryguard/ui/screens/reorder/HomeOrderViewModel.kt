package com.akash.expiryguard.ui.screens.reorder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akash.expiryguard.data.local.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HomeOrderType(val title: String) {
    QUICK_ADD("Quick Add order"),
    CATEGORIES("Category order")
}

class HomeOrderViewModel(
    private val appPreferences: AppPreferences,
    private val orderType: HomeOrderType
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeOrderUiState(title = orderType.title))
    val uiState: StateFlow<HomeOrderUiState> = _uiState.asStateFlow()
    private var loaded = false

    init {
        viewModelScope.launch {
            orderFlow().collect { savedItems ->
                if (!loaded) {
                    loaded = true
                    _uiState.update { it.copy(items = savedItems, isLoading = false) }
                }
            }
        }
    }

    fun moveItem(fromIndex: Int, toIndex: Int) {
        val items = _uiState.value.items.toMutableList()
        if (fromIndex !in items.indices || toIndex !in items.indices) return
        val movedItem = items.removeAt(fromIndex)
        items.add(toIndex, movedItem)
        _uiState.update { it.copy(items = items) }
    }

    fun save(onSaved: () -> Unit) {
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                when (orderType) {
                    HomeOrderType.QUICK_ADD -> appPreferences.updateQuickAddTemplateOrder(_uiState.value.items)
                    HomeOrderType.CATEGORIES -> appPreferences.updateCategoryOrder(_uiState.value.items)
                }
                onSaved()
            } catch (_: Exception) {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun orderFlow() = when (orderType) {
        HomeOrderType.QUICK_ADD -> appPreferences.quickAddTemplateOrder
        HomeOrderType.CATEGORIES -> appPreferences.categoryOrder
    }

    class Factory(
        private val appPreferences: AppPreferences,
        private val orderType: HomeOrderType
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeOrderViewModel(appPreferences, orderType) as T
        }
    }
}

data class HomeOrderUiState(
    val title: String = "",
    val items: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false
)
