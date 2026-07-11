package com.akash.expiryguard.ui.screens.addedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.akash.expiryguard.data.model.ExpiryCategory
import com.akash.expiryguard.util.formatIsoDate
import com.akash.expiryguard.util.parseIsoDate
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private val ReminderOptions = listOf(0, 1, 3, 7, 15, 30)

@Composable
fun AddEditItemScreen(
    viewModel: AddEditItemViewModel,
    isEditing: Boolean,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AddEditItemContent(
        uiState = uiState,
        isEditing = isEditing,
        onNavigateBack = onNavigateBack,
        onNameChange = viewModel::onNameChange,
        onCategoryChange = viewModel::onCategoryChange,
        onExpiryDateChange = viewModel::onExpiryDateChange,
        onPurchaseDateChange = viewModel::onPurchaseDateChange,
        onQuantityChange = viewModel::onQuantityChange,
        onPriceChange = viewModel::onPriceChange,
        onCurrencyChange = viewModel::onCurrencyChange,
        onReminderDaysBeforeChange = viewModel::onReminderDaysBeforeChange,
        onNotesChange = viewModel::onNotesChange,
        onSaveClick = { viewModel.saveItem(onSuccess = onNavigateBack) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditItemContent(
    uiState: AddEditItemUiState,
    isEditing: Boolean,
    onNavigateBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onExpiryDateChange: (String) -> Unit,
    onPurchaseDateChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onReminderDaysBeforeChange: (Int) -> Unit,
    onNotesChange: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    var showExpiryDatePicker by remember { mutableStateOf(false) }
    var showPurchaseDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = if (isEditing) "Edit item" else "Add item") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text(text = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                uiState.loadErrorMessage?.let { message ->
                    item { ErrorText(message = message) }
                }

                item {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = onNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(text = "Name") },
                        isError = uiState.nameError != null,
                        supportingText = { uiState.nameError?.let { Text(text = it) } }
                    )
                }

                item {
                    CategoryDropdown(
                        selectedCategory = uiState.category,
                        onCategoryChange = onCategoryChange
                    )
                }

                item {
                    DateField(
                        label = "Expiry date",
                        value = uiState.expiryDate,
                        isError = uiState.expiryDateError != null,
                        errorText = uiState.expiryDateError,
                        onClick = { showExpiryDatePicker = true }
                    )
                }

                item {
                    DateField(
                        label = "Purchase date",
                        value = uiState.purchaseDate,
                        isError = false,
                        errorText = null,
                        onClick = { showPurchaseDatePicker = true }
                    )
                }

                item {
                    OutlinedTextField(
                        value = uiState.quantity,
                        onValueChange = onQuantityChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(text = "Quantity") }
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.price,
                            onValueChange = onPriceChange,
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text(text = "Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = uiState.priceError != null,
                            supportingText = { uiState.priceError?.let { Text(text = it) } }
                        )
                        OutlinedTextField(
                            value = uiState.currency,
                            onValueChange = onCurrencyChange,
                            modifier = Modifier.weight(0.75f),
                            singleLine = true,
                            label = { Text(text = "Currency") }
                        )
                    }
                }

                item {
                    Text(
                        text = "Reminder",
                        style = MaterialTheme.typography.labelLarge
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ReminderOptions) { days ->
                            FilterChip(
                                selected = uiState.reminderDaysBefore == days,
                                onClick = { onReminderDaysBeforeChange(days) },
                                label = { Text(text = reminderLabel(days)) }
                            )
                        }
                    }
                    uiState.reminderError?.let { ErrorText(message = it) }
                }

                item {
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = onNotesChange,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        label = { Text(text = "Notes") }
                    )
                }

                uiState.saveErrorMessage?.let { message ->
                    item { ErrorText(message = message) }
                }

                item {
                    Button(
                        onClick = onSaveClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSaving
                    ) {
                        Text(text = if (uiState.isSaving) "Saving..." else "Save")
                    }
                }
            }
        }
    }

    if (showExpiryDatePicker) {
        IsoDatePickerDialog(
            initialDate = uiState.expiryDate,
            onDismiss = { showExpiryDatePicker = false },
            onDateSelected = {
                onExpiryDateChange(it)
                showExpiryDatePicker = false
            }
        )
    }

    if (showPurchaseDatePicker) {
        IsoDatePickerDialog(
            initialDate = uiState.purchaseDate,
            onDismiss = { showPurchaseDatePicker = false },
            onDateSelected = {
                onPurchaseDateChange(it)
                showPurchaseDatePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    selectedCategory: String,
    onCategoryChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            singleLine = true,
            label = { Text(text = "Category") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ExpiryCategory.entries.forEach { category ->
                DropdownMenuItem(
                    text = { Text(text = category.displayName) },
                    onClick = {
                        onCategoryChange(category.displayName)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DateField(
    label: String,
    value: String,
    isError: Boolean,
    errorText: String?,
    onClick: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        singleLine = true,
        label = { Text(text = label) },
        placeholder = { Text(text = "yyyy-MM-dd") },
        isError = isError,
        supportingText = { errorText?.let { Text(text = it) } },
        trailingIcon = {
            TextButton(onClick = onClick) {
                Text(text = "Pick")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IsoDatePickerDialog(
    initialDate: String,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val initialMillis = remember(initialDate) {
        parseIsoDate(initialDate)
            ?.atStartOfDay()
            ?.toInstant(ZoneOffset.UTC)
            ?.toEpochMilli()
    }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedDate = datePickerState.selectedDateMillis
                        ?.let { millis ->
                            Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                        }
                        ?: LocalDate.now()
                    onDateSelected(formatIsoDate(selectedDate))
                }
            ) {
                Text(text = "OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun ErrorText(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium
    )
}

private fun reminderLabel(days: Int): String {
    return when (days) {
        0 -> "0 days"
        1 -> "1 day"
        else -> "$days days"
    }
}
