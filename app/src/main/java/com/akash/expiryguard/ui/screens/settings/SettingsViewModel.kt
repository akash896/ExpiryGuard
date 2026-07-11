package com.akash.expiryguard.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akash.expiryguard.data.repository.ExpiryItemRepository
import com.akash.expiryguard.util.parseIsoDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class SettingsViewModel(
    private val repository: ExpiryItemRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun archiveExpiredItemsOlderThan30Days() {
        if (_uiState.value.isArchiving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isArchiving = true, errorMessage = null, successMessage = null) }
            try {
                val cutoff = LocalDate.now().minusDays(30)
                val itemIds = repository.observeItems().first()
                    .filter { item ->
                        !item.archived && parseIsoDate(item.expiryDate)?.isBefore(cutoff) == true
                    }
                    .map { it.id }
                    .filter { it.isNotBlank() }

                itemIds.forEach { itemId ->
                    repository.archiveItem(itemId)
                }
                _uiState.update {
                    it.copy(
                        isArchiving = false,
                        successMessage = if (itemIds.isEmpty()) {
                            "No items need archiving."
                        } else {
                            "Archived ${itemIds.size} expired item(s)."
                        }
                    )
                }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isArchiving = false,
                        errorMessage = error.message ?: "Unable to archive expired items."
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }

    class Factory(
        private val repository: ExpiryItemRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(repository) as T
        }
    }
}

data class SettingsUiState(
    val isArchiving: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
