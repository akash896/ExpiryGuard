package com.akash.expiryguard.ui.screens.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.text.KeyboardOptions
import com.akash.expiryguard.data.model.ExpiryCategory
import com.akash.expiryguard.data.model.ShoppingItem

@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var editorItem by remember { mutableStateOf<ShoppingItem?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<ShoppingItem?>(null) }

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        val message = uiState.errorMessage ?: uiState.successMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    ShoppingListContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onAddItem = {
            editorItem = null
            showEditor = true
        },
        onEditItem = { item ->
            editorItem = item
            showEditor = true
        },
        onCheckedChange = viewModel::setChecked,
        onDeleteItem = { itemToDelete = it }
    )

    if (showEditor) {
        ShoppingItemEditorDialog(
            item = editorItem,
            isSaving = uiState.isSaving,
            onDismiss = { showEditor = false },
            onSave = { item ->
                showEditor = false
                viewModel.saveShoppingItem(item)
            }
        )
    }

    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text(text = "Delete shopping item?") },
            text = { Text(text = "${item.name.ifBlank { "This item" }} will be removed from your shopping list.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemToDelete = null
                        viewModel.deleteShoppingItem(item.id)
                    }
                ) {
                    Text(text = "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) { Text(text = "Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShoppingListContent(
    uiState: ShoppingListUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onAddItem: () -> Unit,
    onEditItem: (ShoppingItem) -> Unit,
    onCheckedChange: (ShoppingItem, Boolean) -> Unit,
    onDeleteItem: (ShoppingItem) -> Unit
) {
    val uncheckedItems = uiState.items.filterNot { it.checked }
    val checkedItems = uiState.items.filter { it.checked }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Shopping list") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) { Text(text = "Back") }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddItem) {
                Text(text = "+", style = MaterialTheme.typography.headlineSmall)
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "To buy",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (uncheckedItems.isEmpty()) {
                        item { EmptyShoppingMessage("No items to buy right now.") }
                    } else {
                        items(uncheckedItems, key = { it.id }) { item ->
                            ShoppingItemCard(
                                item = item,
                                onCheckedChange = { checked -> onCheckedChange(item, checked) },
                                onEdit = { onEditItem(item) },
                                onDelete = { onDeleteItem(item) }
                            )
                        }
                    }

                    item {
                        Text(
                            text = "Checked",
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (checkedItems.isEmpty()) {
                        item { EmptyShoppingMessage("No checked items yet.") }
                    } else {
                        items(checkedItems, key = { it.id }) { item ->
                            ShoppingItemCard(
                                item = item,
                                onCheckedChange = { checked -> onCheckedChange(item, checked) },
                                onEdit = { onEditItem(item) },
                                onDelete = { onDeleteItem(item) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShoppingItemCard(
    item: ShoppingItem,
    onCheckedChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(checked = item.checked, onCheckedChange = onCheckedChange)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = item.name.ifBlank { "Unnamed item" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = listOfNotNull(
                        item.category.takeIf { it.isNotBlank() },
                        item.quantity.takeIf { it.isNotBlank() },
                        item.estimatedPrice.takeIf { it.isFinite() && it > 0.0 }
                            ?.let { formatMoney(it, item.currency) }
                    ).joinToString(" · ").ifBlank { "No details" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onEdit) { Text(text = "Edit") }
                    TextButton(onClick = onDelete) {
                        Text(text = "Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyShoppingMessage(message: String) {
    Text(
        text = message,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ShoppingItemEditorDialog(
    item: ShoppingItem?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (ShoppingItem) -> Unit
) {
    var name by remember(item?.id) { mutableStateOf(item?.name.orEmpty()) }
    var category by remember(item?.id) {
        mutableStateOf(item?.category.orEmpty().ifBlank { ExpiryCategory.OTHER.displayName })
    }
    var quantity by remember(item?.id) { mutableStateOf(item?.quantity.orEmpty()) }
    var price by remember(item?.id) {
        mutableStateOf(item?.estimatedPrice?.takeIf { it > 0.0 && it.isFinite() }?.toString().orEmpty())
    }
    var currency by remember(item?.id) { mutableStateOf(item?.currency.orEmpty().ifBlank { "INR" }) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (item == null) "Add shopping item" else "Edit shopping item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(text = "Name") },
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(text = it) } }
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(text = "Category") }
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(text = "Quantity") }
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it; priceError = null },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(text = "Estimated price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = priceError != null,
                    supportingText = { priceError?.let { Text(text = it) } }
                )
                OutlinedTextField(
                    value = currency,
                    onValueChange = { currency = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(text = "Currency") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedPrice = price.toDoubleOrNull()
                    nameError = if (name.isBlank()) "Name is required." else null
                    priceError = when {
                        price.isBlank() -> null
                        parsedPrice == null || !parsedPrice.isFinite() -> "Enter a valid price."
                        parsedPrice < 0.0 -> "Price cannot be negative."
                        else -> null
                    }
                    if (nameError == null && priceError == null) {
                        onSave(
                            (item ?: ShoppingItem()).copy(
                                name = name.trim(),
                                category = category.trim().ifBlank { ExpiryCategory.OTHER.displayName },
                                quantity = quantity.trim(),
                                estimatedPrice = parsedPrice ?: 0.0,
                                currency = currency.trim().ifBlank { "INR" }
                            )
                        )
                    }
                },
                enabled = !isSaving
            ) {
                Text(text = if (isSaving) "Saving..." else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text(text = "Cancel") }
        }
    )
}

private fun formatMoney(price: Double, currency: String): String {
    val safePrice = price.takeIf { it.isFinite() && it > 0.0 } ?: 0.0
    return if (currency.equals("INR", ignoreCase = true) || currency.isBlank()) {
        "₹%.2f".format(safePrice)
    } else {
        "${currency.uppercase()} %.2f".format(safePrice)
    }
}
