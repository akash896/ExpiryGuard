package com.akash.expiryguard.ui.screens.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import com.akash.expiryguard.data.model.ExpiryCategory
import com.akash.expiryguard.data.model.ExpiryItem
import com.akash.expiryguard.data.model.ExpiryStatus
import com.akash.expiryguard.data.model.QuickAddTemplate
import com.akash.expiryguard.data.model.QuickAddTemplates
import com.akash.expiryguard.notifications.NotificationHelper
import com.akash.expiryguard.util.daysUntilExpiry
import com.akash.expiryguard.util.getExpiryStatus
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddItemClick: () -> Unit,
    onQuickAddClick: (QuickAddTemplate) -> Unit,
    onItemClick: (String) -> Unit,
    onShoppingListClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onExpenseInsightsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var notificationsAllowed by remember { mutableStateOf(NotificationHelper.canPostNotifications(context)) }
    var pendingNotificationItem by remember { mutableStateOf<ExpiryItem?>(null) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsAllowed = NotificationHelper.canPostNotifications(context)
        val item = pendingNotificationItem
        pendingNotificationItem = null
        if (granted && notificationsAllowed && item != null) {
            NotificationHelper.setRemindersEnabledLocally(context, item.id, true)
            viewModel.setNotificationsEnabled(item, true)
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Allow notifications before enabling item reminders.")
            }
        }
    }

    HomeScreenContent(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onCategorySelected = viewModel::onCategorySelected,
        onNotificationEnabledChange = { item, enabled ->
            if (enabled && !notificationsAllowed) {
                if (NotificationHelper.canRequestNotificationPermission(context)) {
                    pendingNotificationItem = item
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Turn on notifications in system Settings before enabling reminders.")
                    }
                }
            } else {
                NotificationHelper.setRemindersEnabledLocally(context, item.id, enabled)
                viewModel.setNotificationsEnabled(item, enabled)
            }
        },
        onAddItemClick = onAddItemClick,
        onQuickAddClick = onQuickAddClick,
        onItemClick = onItemClick,
        onShoppingListClick = onShoppingListClick,
        onCalendarClick = onCalendarClick,
        onSettingsClick = onSettingsClick,
        onExpenseInsightsClick = onExpenseInsightsClick,
        notificationsAllowed = notificationsAllowed,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onNotificationEnabledChange: (ExpiryItem, Boolean) -> Unit,
    onAddItemClick: () -> Unit,
    onQuickAddClick: (QuickAddTemplate) -> Unit,
    onItemClick: (String) -> Unit,
    onShoppingListClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onExpenseInsightsClick: () -> Unit,
    notificationsAllowed: Boolean,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "ExpiryGuard") },
                actions = {
                    TextButton(onClick = onCalendarClick) {
                        Text(text = "Calendar")
                    }
                    TextButton(onClick = onExpenseInsightsClick) {
                        Text(text = "Insights")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ,
        floatingActionButton = {
            FloatingActionButton(onClick = onAddItemClick) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add item")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                MoneySummarySection(uiState = uiState)
            }

            item {
                QuickAddSection(onTemplateClick = onQuickAddClick)
            }

            item {
                OutlinedButton(
                    onClick = onShoppingListClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Shopping list")
                }
            }

            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(text = "Search by item name") }
                )
            }

            item {
                CategoryFilterChips(
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = onCategorySelected
                )
            }

            when {
                uiState.isLoading -> {
                    item { LoadingState() }
                }

                uiState.errorMessage != null -> {
                    item { ErrorState(message = uiState.errorMessage) }
                }

                uiState.items.isEmpty() -> {
                    item { EmptyState() }
                }

                else -> {
                    groupedItems(uiState.items).forEach { section ->
                        if (section.items.isNotEmpty()) {
                            item {
                                Text(
                                    text = section.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            items(
                                items = section.items,
                                key = { item -> item.id.ifBlank { item.name + item.expiryDate } }
                            ) { item ->
                                ExpiryItemCard(
                                    item = item,
                                    onClick = { onItemClick(item.id) },
                                    notificationsAllowed = notificationsAllowed,
                                    onNotificationEnabledChange = { enabled ->
                                        onNotificationEnabledChange(item, enabled)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAddSection(onTemplateClick: (QuickAddTemplate) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Quick add",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(QuickAddTemplates.all, key = { it.templateId }) { template ->
                AssistChip(
                    onClick = { onTemplateClick(template) },
                    label = { Text(text = template.displayName) }
                )
            }
        }
    }
}

@Composable
private fun MoneySummarySection(uiState: HomeUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Money summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            SummaryRow(
                leftLabel = "Active value",
                leftValue = formatMoney(uiState.totalActiveValue),
                rightLabel = "Expired value",
                rightValue = formatMoney(uiState.totalExpiredValue)
            )
            SummaryRow(
                leftLabel = "Consumed value",
                leftValue = formatMoney(uiState.totalConsumedValue),
                rightLabel = "This month",
                rightValue = formatMoney(uiState.currentMonthSpending)
            )
        }
    }
}

@Composable
private fun SummaryRow(
    leftLabel: String,
    leftValue: String,
    rightLabel: String,
    rightValue: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryMetric(
            label = leftLabel,
            value = leftValue,
            modifier = Modifier.weight(1f)
        )
        SummaryMetric(
            label = rightLabel,
            value = rightValue,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun CategoryFilterChips(
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text(text = "All") }
            )
        }
        items(ExpiryCategory.entries.toList()) { category ->
            FilterChip(
                selected = selectedCategory == category.displayName,
                onClick = { onCategorySelected(category.displayName) },
                label = { Text(text = category.displayName) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpiryItemCard(
    item: ExpiryItem,
    onClick: () -> Unit,
    notificationsAllowed: Boolean,
    onNotificationEnabledChange: (Boolean) -> Unit
) {
    val status = getExpiryStatus(item.expiryDate)
    val isExpiredWaste = status == ExpiryStatus.EXPIRED && !item.consumed
    val isExpiredCategory = item.category == ExpiryCategory.EXPIRED.displayName ||
        status == ExpiryStatus.EXPIRED

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = if (isExpiredWaste) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name.ifBlank { "Unnamed item" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isExpiredCategory) {
                            ExpiryCategory.EXPIRED.displayName
                        } else {
                            item.category.ifBlank { "Other" }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Switch(
                            checked = item.notificationsEnabled && notificationsAllowed,
                            onCheckedChange = onNotificationEnabledChange
                        )
                        Text(
                            text = "Notify",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Text(
                        text = item.expiryDate.ifBlank { "No date" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = itemDetailText(item),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = daysUntilText(item),
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isExpiredWaste) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            if (item.consumed || isExpiredWaste) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (item.consumed) {
                        StatusPill(text = "Consumed")
                    }
                    if (isExpiredWaste) {
                        StatusPill(text = "Expired value ${formatMoney(item.price)}")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "No active items yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Add an item to start tracking expiry dates and spending.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Loading items...")
    }
}

@Composable
private fun ErrorState(message: String) {
    Text(
        text = message,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium
    )
}

private data class HomeSection(
    val title: String,
    val items: List<ExpiryItem>
)

private fun groupedItems(items: List<ExpiryItem>): List<HomeSection> {
    val expiredItems = items.filter(::isExpiredCategory)
    val remainingItems = items - expiredItems.toSet()
    return listOf(
        HomeSection("Expired", expiredItems),
        HomeSection("Expiring Today", remainingItems.filter { getExpiryStatus(it.expiryDate) == ExpiryStatus.TODAY }),
        HomeSection("Expiring This Week", remainingItems.filter { getExpiryStatus(it.expiryDate) == ExpiryStatus.THIS_WEEK }),
        HomeSection("Expiring This Month", remainingItems.filter { getExpiryStatus(it.expiryDate) == ExpiryStatus.THIS_MONTH }),
        HomeSection("Safe for Later", remainingItems.filter { getExpiryStatus(it.expiryDate) == ExpiryStatus.SAFE })
    )
}

private fun isExpiredCategory(item: ExpiryItem): Boolean {
    return item.category == ExpiryCategory.EXPIRED.displayName ||
        getExpiryStatus(item.expiryDate) == ExpiryStatus.EXPIRED
}

private fun itemDetailText(item: ExpiryItem): String {
    return listOfNotNull(
        item.quantity.takeIf { it.isNotBlank() },
        item.price.takeIf { it > 0.0 && it.isFinite() }?.let { formatMoney(it) }
    ).joinToString(separator = " - ").ifBlank { "No quantity or price" }
}

private fun daysUntilText(item: ExpiryItem): String {
    return when (val days = daysUntilExpiry(item.expiryDate)) {
        null -> "No expiry"
        in Long.MIN_VALUE..-1L -> "${-days} days expired"
        0L -> "Expires today"
        1L -> "Expires tomorrow"
        else -> "$days days left"
    }
}

private fun formatMoney(value: Double): String {
    val safeValue = if (value.isFinite() && value > 0.0) value else 0.0
    return "₹%.2f".format(safeValue)
}
