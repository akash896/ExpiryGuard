package com.akash.expiryguard.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akash.expiryguard.data.model.ExpiryItem
import com.akash.expiryguard.data.repository.ExpiryItemRepository
import com.akash.expiryguard.util.formatIsoDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class ItemDetailViewModel(
    private val repository: ExpiryItemRepository,
    private val itemId: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(ItemDetailUiState(isLoading = true))
    val uiState: StateFlow<ItemDetailUiState> = _uiState.asStateFlow()

    init {
        refreshItem()
    }

    fun refreshItem() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, actionErrorMessage = null) }
            try {
                val item = repository.getItem(itemId)
                _uiState.value = ItemDetailUiState(
                    item = item,
                    isLoading = false,
                    errorMessage = if (item == null) "Item was not found." else null
                )
            } catch (error: Exception) {
                _uiState.value = ItemDetailUiState(
                    isLoading = false,
                    errorMessage = error.message ?: "Unable to load item."
                )
            }
        }
    }

    fun archiveItem(onSuccess: () -> Unit) = runAction(onSuccess) {
        repository.archiveItem(itemId)
    }

    fun deleteItem(onSuccess: () -> Unit) = runAction(onSuccess) {
        repository.deleteItem(itemId)
    }

    fun markItemConsumed() = runAction(onSuccess = ::refreshItem) {
        repository.markItemConsumed(itemId, formatIsoDate(LocalDate.now()))
    }

    fun markItemNotConsumed() = runAction(onSuccess = ::refreshItem) {
        repository.markItemNotConsumed(itemId)
    }

    private fun runAction(onSuccess: () -> Unit, action: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true, actionErrorMessage = null) }
            try {
                action()
                _uiState.update { it.copy(isActionInProgress = false) }
                onSuccess()
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isActionInProgress = false,
                        actionErrorMessage = error.message ?: "Unable to update item."
                    )
                }
            }
        }
    }

    class Factory(
        private val repository: ExpiryItemRepository,
        private val itemId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ItemDetailViewModel(repository, itemId) as T
        }
    }
}

data class ItemDetailUiState(
    val item: ExpiryItem? = null,
    val isLoading: Boolean = false,
    val isActionInProgress: Boolean = false,
    val errorMessage: String? = null,
    val actionErrorMessage: String? = null
)
