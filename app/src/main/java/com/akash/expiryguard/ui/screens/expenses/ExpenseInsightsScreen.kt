package com.akash.expiryguard.ui.screens.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.clip
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
        onPreviousPeriod = viewModel::showPreviousPeriod,
        onNextPeriod = viewModel::showNextPeriod,
        onCategorySortSelected = viewModel::onCategorySortSelected,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseInsightsContent(
    uiState: ExpenseInsightsUiState,
    onPeriodSelected: (ExpensePeriod) -> Unit,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onCategorySortSelected: (ExpenseCategorySort) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Expense Insights") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) { Text(text = "Back") }
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
            item {
                PeriodNavigation(
                    periodLabel = uiState.periodLabel,
                    onPreviousPeriod = onPreviousPeriod,
                    onNextPeriod = onNextPeriod
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
                    val summary = requireNotNull(uiState.summary)
                    item {
                        SummaryCards(
                            summary = summary,
                            wastePercentage = uiState.wastePercentage
                        )
                    }
                    item {
                        CategoryRanking(
                            highestSpendingCategory = uiState.highestSpendingCategory,
                            highestExpiredValueCategory = uiState.highestExpiredValueCategory
                        )
                    }
                    item {
                        Text(
                            text = "Category breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    item {
                        CategorySortSelector(
                            selectedSort = uiState.selectedCategorySort,
                            onSortSelected = onCategorySortSelected
                        )
                    }

                    val highestSpent = uiState.sortedCategories.maxOfOrNull { it.totalSpent } ?: 0.0
                    items(uiState.sortedCategories, key = { it.category }) { categorySummary ->
                        CategoryExpenseBreakdown(
                            summary = categorySummary,
                            highestSpent = highestSpent
                        )
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
private fun PeriodNavigation(
    periodLabel: String,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onPreviousPeriod) { Text(text = "Previous") }
        Text(
            text = periodLabel,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        TextButton(onClick = onNextPeriod) { Text(text = "Next") }
    }
}

@Composable
private fun SummaryCards(summary: ExpenseSummary, wastePercentage: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
            leftLabel = "Waste percentage",
            leftValue = formatPercentage(wastePercentage),
            rightLabel = "Items",
            rightValue = summary.itemCount.toString()
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
        SummaryCard(leftLabel, leftValue, Modifier.weight(1f))
        SummaryCard(rightLabel, rightValue, Modifier.weight(1f))
    }
}

@Composable
private fun SummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
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
private fun CategoryRanking(
    highestSpendingCategory: CategoryExpenseSummary?,
    highestExpiredValueCategory: CategoryExpenseSummary?
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Category leaders",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        RankingRow(
            label = "Highest spending",
            value = highestSpendingCategory?.let { "${it.category} · ${formatMoney(it.totalSpent)}" }
                ?: "No category data"
        )
        RankingRow(
            label = "Highest expired value",
            value = highestExpiredValueCategory?.let { "${it.category} · ${formatMoney(it.totalExpiredValue)}" }
                ?: "No category data"
        )
    }
}

@Composable
private fun RankingRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CategorySortSelector(
    selectedSort: ExpenseCategorySort,
    onSortSelected: (ExpenseCategorySort) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(ExpenseCategorySort.entries.toList()) { sort ->
            FilterChip(
                selected = selectedSort == sort,
                onClick = { onSortSelected(sort) },
                label = { Text(text = sort.label) }
            )
        }
    }
}

@Composable
fun CategoryExpenseBreakdown(summary: CategoryExpenseSummary, highestSpent: Double) {
    val spendingFraction = if (highestSpent.isFinite() && highestSpent > 0.0) {
        (summary.totalSpent / highestSpent).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

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
            SpendingBar(spendingFraction)
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
private fun SpendingBar(fraction: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .height(8.dp)
                .background(MaterialTheme.colorScheme.primary)
        )
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
private fun BreakdownMetric(label: String, value: String, modifier: Modifier = Modifier) {
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

private fun formatPercentage(value: Double): String {
    val safeValue = if (value.isFinite() && value >= 0.0) value else 0.0
    return "%.1f%%".format(safeValue)
}
