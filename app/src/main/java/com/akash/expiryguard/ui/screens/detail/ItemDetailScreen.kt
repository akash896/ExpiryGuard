package com.akash.expiryguard.ui.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.akash.expiryguard.data.model.ExpiryItem
import com.akash.expiryguard.data.model.ExpiryStatus
import com.akash.expiryguard.util.daysUntilExpiry
import com.akash.expiryguard.util.getExpiryStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ItemDetailScreen(
    viewModel: ItemDetailViewModel,
    onNavigateBack: () -> Unit,
    onEditItem: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteConfirmation by mutableStateOf(false)
    var showArchiveConfirmation by mutableStateOf(false)

    ItemDetailContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onEditItem = onEditItem,
        onDeleteItem = { showDeleteConfirmation = true },
        onArchiveItem = { showArchiveConfirmation = true },
        onMarkConsumed = viewModel::markItemConsumed,
        onMarkNotConsumed = viewModel::markItemNotConsumed,
        onRetry = viewModel::refreshItem
    )

    if (showDeleteConfirmation) {
        ConfirmationDialog(
            title = "Delete item?",
            message = "This permanently removes the item from your account.",
            confirmLabel = "Delete",
            onDismiss = { showDeleteConfirmation = false },
            onConfirm = {
                showDeleteConfirmation = false
                viewModel.deleteItem(onSuccess = onNavigateBack)
            }
        )
    }

    if (showArchiveConfirmation) {
        ConfirmationDialog(
            title = "Archive item?",
            message = "Archived items are hidden from the Home screen but remain in your account.",
            confirmLabel = "Archive",
            onDismiss = { showArchiveConfirmation = false },
            onConfirm = {
                showArchiveConfirmation = false
                viewModel.archiveItem(onSuccess = onNavigateBack)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemDetailContent(
    uiState: ItemDetailUiState,
    onNavigateBack: () -> Unit,
    onEditItem: (String) -> Unit,
    onDeleteItem: () -> Unit,
    onArchiveItem: () -> Unit,
    onMarkConsumed: () -> Unit,
    onMarkNotConsumed: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Item details") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) { Text(text = "Back") }
                },
                actions = {
                    uiState.item?.let { item ->
                        TextButton(onClick = { onEditItem(item.id) }) { Text(text = "Edit") }
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingContent(modifier = Modifier.padding(innerPadding))
            uiState.item == null -> ErrorContent(
                message = uiState.errorMessage ?: "Item was not found.",
                modifier = Modifier.padding(innerPadding),
                onRetry = onRetry
            )
            else -> ItemDetailBody(
                item = uiState.item,
                isActionInProgress = uiState.isActionInProgress,
                actionErrorMessage = uiState.actionErrorMessage,
                modifier = Modifier.padding(innerPadding),
                onDeleteItem = onDeleteItem,
                onArchiveItem = onArchiveItem,
                onMarkConsumed = onMarkConsumed,
                onMarkNotConsumed = onMarkNotConsumed
            )
        }
    }
}

@Composable
private fun ItemDetailBody(
    item: ExpiryItem,
    isActionInProgress: Boolean,
    actionErrorMessage: String?,
    modifier: Modifier = Modifier,
    onDeleteItem: () -> Unit,
    onArchiveItem: () -> Unit,
    onMarkConsumed: () -> Unit,
    onMarkNotConsumed: () -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = item.name.ifBlank { "Unnamed item" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        item { MoneyInsight(item = item) }

        item {
            DetailCard {
                DetailRow("Category", item.category.ifBlank { "Other" })
                DetailRow("Expiry date", item.expiryDate.ifBlank { "Not set" })
                DetailRow("Time remaining", expiryDescription(item.expiryDate))
                DetailRow("Purchase date", item.purchaseDate.ifBlank { "Not set" })
                DetailRow("Quantity", item.quantity.ifBlank { "Not set" })
                DetailRow("Price", formatMoney(item.price, item.currency))
                DetailRow("Reminder", reminderDescription(item.reminderDaysBefore))
                DetailRow("Consumed", consumedDescription(item))
                DetailRow("Archived", if (item.archived) "Yes" else "No")
                DetailRow("Created", formatTimestamp(item.createdAt))
                DetailRow("Updated", formatTimestamp(item.updatedAt))
            }
        }

        if (item.notes.isNotBlank()) {
            item {
                DetailCard {
                    Text(text = "Notes", style = MaterialTheme.typography.titleSmall)
                    Text(text = item.notes)
                }
            }
        }

        actionErrorMessage?.let { message ->
            item {
                Text(text = message, color = MaterialTheme.colorScheme.error)
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (item.consumed) {
                    OutlinedButton(
                        onClick = onMarkNotConsumed,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isActionInProgress
                    ) {
                        Text(text = "Mark as not consumed")
                    }
                } else {
                    Button(
                        onClick = onMarkConsumed,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isActionInProgress
                    ) {
                        Text(text = "Mark as consumed")
                    }
                }

                if (!item.archived) {
                    OutlinedButton(
                        onClick = onArchiveItem,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isActionInProgress
                    ) {
                        Text(text = "Archive item")
                    }
                }

                TextButton(
                    onClick = onDeleteItem,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isActionInProgress
                ) {
                    Text(text = "Delete item", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun MoneyInsight(item: ExpiryItem) {
    val status = getExpiryStatus(item.expiryDate)
    val label = when {
        item.consumed -> "Consumed value"
        status == ExpiryStatus.EXPIRED -> "Expired value"
        else -> "Active item value"
    }
    val color = when {
        item.consumed -> MaterialTheme.colorScheme.secondaryContainer
        status == ExpiryStatus.EXPIRED -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.labelLarge)
            Text(
                text = formatMoney(item.price, item.currency),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DetailCard(content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(message: String, modifier: Modifier = Modifier, onRetry: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
        TextButton(onClick = onRetry) { Text(text = "Retry") }
    }
}

@Composable
private fun ConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(text = confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(text = "Cancel") } }
    )
}

private fun expiryDescription(expiryDate: String): String {
    return when (val days = daysUntilExpiry(expiryDate)) {
        null -> "Unknown"
        0L -> "Expires today"
        in Long.MIN_VALUE..-1L -> "Expired ${-days} days ago"
        else -> "Expires in $days days"
    }
}

private fun consumedDescription(item: ExpiryItem): String {
    return if (item.consumed) {
        item.consumedAt.takeIf { it.isNotBlank() }?.let { "Yes, $it" } ?: "Yes"
    } else {
        "No"
    }
}

private fun reminderDescription(days: Int): String = when (days) {
    0 -> "On expiry day"
    1 -> "1 day before"
    else -> "$days days before"
}

private fun formatMoney(amount: Double, currency: String): String {
    val safeAmount = amount.takeIf { it.isFinite() && it >= 0.0 } ?: 0.0
    val symbol = if (currency.ifBlank { "INR" }.equals("INR", ignoreCase = true)) "₹" else "${currency.uppercase()} "
    return String.format(Locale.US, "%s%.2f", symbol, safeAmount)
}

private fun formatTimestamp(timestamp: Long): String {
    if (timestamp <= 0L) return "Not available"
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
}
