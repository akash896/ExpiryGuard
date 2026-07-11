package com.akash.expiryguard.ui.screens.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akash.expiryguard.data.model.ExpiryCategory
import com.akash.expiryguard.data.model.ExpiryItem
import com.akash.expiryguard.data.repository.ExpiryItemRepository
import com.akash.expiryguard.util.formatIsoDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddEditItemViewModel(
    private val repository: ExpiryItemRepository,
    private val itemId: String?
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddEditItemUiState(isLoading = !itemId.isNullOrBlank()))
    val uiState: StateFlow<AddEditItemUiState> = _uiState.asStateFlow()

    init {
        if (!itemId.isNullOrBlank()) {
            loadItem(itemId)
        }
    }

    fun onNameChange(value: String) = updateState { copy(name = value, nameError = null) }
    fun onCategoryChange(value: String) = updateState { copy(category = value) }
    fun onExpiryDateChange(value: String) = updateState { copy(expiryDate = value, expiryDateError = null) }
    fun onPurchaseDateChange(value: String) = updateState { copy(purchaseDate = value) }
    fun onQuantityChange(value: String) = updateState { copy(quantity = value) }
    fun onPriceChange(value: String) = updateState { copy(price = value, priceError = null) }
    fun onCurrencyChange(value: String) = updateState { copy(currency = value.ifBlank { "INR" }) }
    fun onReminderDaysBeforeChange(value: Int) = updateState { copy(reminderDaysBefore = value, reminderError = null) }
    fun onNotificationsEnabledChange(value: Boolean) = updateState { copy(notificationsEnabled = value) }
    fun onNotesChange(value: String) = updateState { copy(notes = value) }

    fun saveItem(onSuccess: () -> Unit) {
        val current = _uiState.value
        val validatedState = current.validate()
        _uiState.value = validatedState
        if (!validatedState.canSave) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveErrorMessage = null) }
            try {
                val now = System.currentTimeMillis()
                val item = ExpiryItem(
                    id = itemId.orEmpty(),
                    name = validatedState.name.trim(),
                    category = validatedState.category,
                    expiryDate = validatedState.expiryDate,
                    purchaseDate = validatedState.purchaseDate.ifBlank { formatIsoDate(LocalDate.now()) },
                    quantity = validatedState.quantity.trim(),
                    price = validatedState.price.toDoubleOrNull()
                        ?.takeIf { it.isFinite() && it >= 0.0 }
                        ?: 0.0,
                    currency = validatedState.currency.ifBlank { "INR" },
                    notes = validatedState.notes.trim(),
                    reminderDaysBefore = validatedState.reminderDaysBefore,
                    notificationsEnabled = validatedState.notificationsEnabled,
                    createdAt = validatedState.createdAt,
                    updatedAt = now,
                    archived = validatedState.archived,
                    consumed = validatedState.consumed,
                    consumedAt = validatedState.consumedAt
                )

                if (itemId.isNullOrBlank()) {
                    repository.addItem(item)
                } else {
                    repository.updateItem(item)
                }
                onSuccess()
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveErrorMessage = error.message ?: "Unable to save item."
                    )
                }
            }
        }
    }

    private fun loadItem(itemId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadErrorMessage = null) }
            try {
                val item = repository.getItem(itemId)
                if (item == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadErrorMessage = "Item was not found."
                        )
                    }
                } else {
                    _uiState.value = AddEditItemUiState.fromItem(item)
                }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadErrorMessage = error.message ?: "Unable to load item."
                    )
                }
            }
        }
    }

    private fun updateState(transform: AddEditItemUiState.() -> AddEditItemUiState) {
        _uiState.update { it.transform() }
    }

    class Factory(
        private val repository: ExpiryItemRepository,
        private val itemId: String?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddEditItemViewModel(repository, itemId) as T
        }
    }
}

data class AddEditItemUiState(
    val name: String = "",
    val category: String = ExpiryCategory.OTHER.displayName,
    val expiryDate: String = "",
    val purchaseDate: String = "",
    val quantity: String = "",
    val price: String = "",
    val currency: String = "INR",
    val reminderDaysBefore: Int = 1,
    val notificationsEnabled: Boolean = true,
    val notes: String = "",
    val createdAt: Long = 0L,
    val archived: Boolean = false,
    val consumed: Boolean = false,
    val consumedAt: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val loadErrorMessage: String? = null,
    val saveErrorMessage: String? = null,
    val nameError: String? = null,
    val expiryDateError: String? = null,
    val priceError: String? = null,
    val reminderError: String? = null
) {
    val canSave: Boolean
        get() = nameError == null &&
            expiryDateError == null &&
            priceError == null &&
            reminderError == null

    fun validate(): AddEditItemUiState {
        val parsedPrice = price.toDoubleOrNull()
        return copy(
            nameError = if (name.isBlank()) "Name is required." else null,
            expiryDateError = if (expiryDate.isBlank()) "Expiry date is required." else null,
            priceError = when {
                price.isBlank() -> null
                parsedPrice == null || !parsedPrice.isFinite() -> "Enter a valid price."
                parsedPrice < 0.0 -> "Price cannot be negative."
                else -> null
            },
            reminderError = if (reminderDaysBefore < 0) "Reminder days cannot be negative." else null,
            currency = currency.ifBlank { "INR" }
        )
    }

    companion object {
        fun fromItem(item: ExpiryItem): AddEditItemUiState {
            return AddEditItemUiState(
                name = item.name,
                category = item.category.ifBlank { ExpiryCategory.OTHER.displayName },
                expiryDate = item.expiryDate,
                purchaseDate = item.purchaseDate,
                quantity = item.quantity,
                price = item.price.takeIf { it > 0.0 && it.isFinite() }?.toString().orEmpty(),
                currency = item.currency.ifBlank { "INR" },
                reminderDaysBefore = item.reminderDaysBefore,
                notes = item.notes,
                createdAt = item.createdAt,
                notificationsEnabled = item.notificationsEnabled,
                archived = item.archived,
                consumed = item.consumed,
                consumedAt = item.consumedAt,
                isLoading = false
            )
        }
    }
}
