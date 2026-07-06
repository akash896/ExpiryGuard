package com.akash.expiryguard.ui.screens.expenses

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.akash.expiryguard.data.model.CategoryExpenseSummary
import com.akash.expiryguard.data.model.ExpensePeriod
import com.akash.expiryguard.data.model.ExpenseSummary

@Composable
fun ExpenseInsightsScreen(
    viewModel: ExpenseInsightsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ExpenseInsightsContent(
        uiState = uiState,
        onPeriodSelected = viewModel::onPeriodSelected,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseInsightsContent(
    uiState: ExpenseInsightsUiState,
    onPeriodSelected: (ExpensePeriod) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Expense Insights") },
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
            item {
                ExpensePeriodSelector(
                    selectedPeriod = uiState.selectedPeriod,
                    onPeriodSelected = onPeriodSelected
                )
            }

            when {
                uiState.isLoading -> {
                    item { LoadingState() }
                }

                uiState.errorMessage != null -> {
                    item { ErrorState(message = uiState.errorMessage) }
                }

                uiState.summary == null || uiState.summary.itemCount == 0 -> {
                    item { EmptyInsightsState() }
                }

                else -> {
                    item {
                        SummaryCards(summary = uiState.summary)
                    }

                    item {
                        Text(
                            text = "Category breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    items(
                        items = uiState.summary.categoryBreakdown.values
                            .sortedByDescending { it.totalSpent },
                        key = { it.category }
                    ) { categorySummary ->
                        CategoryExpenseBreakdown(summary = categorySummary)
                    }
                }
            }
        }
    }
}

@Composable
fun ExpensePeriodSelector(
    selectedPeriod: ExpensePeriod,
    onPeriodSelected: (ExpensePeriod) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(ExpensePeriod.entries.toList()) { period ->
            FilterChip(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                label = { Text(text = period.label()) }
            )
        }
    }
}

@Composable
private fun SummaryCards(summary: ExpenseSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = summary.periodLabel,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        SummaryRow(
            leftLabel = "Total spent",
            leftValue = formatMoney(summary.totalSpent),
            rightLabel = "Expired value",
            rightValue = formatMoney(summary.totalExpiredValue)
        )
        SummaryRow(
            leftLabel = "Consumed value",
            leftValue = formatMoney(summary.totalConsumedValue),
            rightLabel = "Active value",
            rightValue = formatMoney(summary.totalActiveValue)
        )
        SummaryRow(
            leftLabel = "Items",
            leftValue = summary.itemCount.toString(),
            rightLabel = "Expired items",
            rightValue = summary.expiredItemCount.toString()
        )
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
        SummaryCard(
            label = leftLabel,
            value = leftValue,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            label = rightLabel,
            value = rightValue,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun CategoryExpenseBreakdown(summary: CategoryExpenseSummary) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = summary.category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${summary.itemCount} items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            BreakdownRow(
                leftLabel = "Spent",
                leftValue = formatMoney(summary.totalSpent),
                rightLabel = "Expired",
                rightValue = formatMoney(summary.totalExpiredValue)
            )
            BreakdownRow(
                leftLabel = "Consumed",
                leftValue = formatMoney(summary.totalConsumedValue),
                rightLabel = "Active",
                rightValue = formatMoney(summary.totalActiveValue)
            )
        }
    }
}

@Composable
private fun BreakdownRow(
    leftLabel: String,
    leftValue: String,
    rightLabel: String,
    rightValue: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BreakdownMetric(leftLabel, leftValue, Modifier.weight(1f))
        BreakdownMetric(rightLabel, rightValue, Modifier.weight(1f))
    }
}

@Composable
private fun BreakdownMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
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
        Text(text = "Loading insights...")
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

@Composable
private fun EmptyInsightsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "No expenses for this period",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Items with a purchase date or expiry date will appear here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun ExpensePeriod.label(): String {
    return when (this) {
        ExpensePeriod.DAILY -> "Daily"
        ExpensePeriod.MONTHLY -> "Monthly"
        ExpensePeriod.QUARTERLY -> "Quarterly"
        ExpensePeriod.ANNUALLY -> "Annually"
    }
}

private fun formatMoney(value: Double): String {
    val safeValue = if (value.isFinite() && value > 0.0) value else 0.0
    return "₹%.2f".format(safeValue)
}
