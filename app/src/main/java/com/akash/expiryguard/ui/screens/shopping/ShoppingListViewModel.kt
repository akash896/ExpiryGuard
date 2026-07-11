package com.akash.expiryguard.ui.screens.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akash.expiryguard.data.model.ShoppingItem
import com.akash.expiryguard.data.repository.ShoppingListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ShoppingListViewModel(
    private val repository: ShoppingListRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ShoppingListUiState(isLoading = true))
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeShoppingItems()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Unable to load shopping items."
                        )
                    }
                }
                .collect { items ->
                    _uiState.update {
                        it.copy(items = items, isLoading = false, errorMessage = null)
                    }
                }
        }
    }

    fun saveShoppingItem(item: ShoppingItem) {
        if (item.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name is required.") }
            return
        }

        runAction(
            successMessage = if (item.id.isBlank()) "Shopping item added." else "Shopping item updated."
        ) {
            if (item.id.isBlank()) {
                repository.addShoppingItem(item)
            } else {
                repository.updateShoppingItem(item)
            }
        }
    }

    fun setChecked(item: ShoppingItem, checked: Boolean) {
        if (item.id.isBlank() || item.checked == checked) return
        runAction(successMessage = null) {
            repository.setShoppingItemChecked(item.id, checked)
        }
    }

    fun deleteShoppingItem(itemId: String) {
        if (itemId.isBlank()) return
        runAction(successMessage = "Shopping item deleted.") {
            repository.deleteShoppingItem(itemId)
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun runAction(successMessage: String?, action: suspend () -> Unit) {
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
            try {
                action()
                _uiState.update { it.copy(isSaving = false, successMessage = successMessage) }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "Unable to update shopping list."
                    )
                }
            }
        }
    }

    class Factory(
        private val repository: ShoppingListRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ShoppingListViewModel(repository) as T
        }
    }
}

data class ShoppingListUiState(
    val items: List<ShoppingItem> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
