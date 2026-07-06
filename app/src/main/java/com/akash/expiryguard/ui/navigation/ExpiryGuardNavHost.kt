package com.akash.expiryguard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.akash.expiryguard.data.repository.ExpiryItemRepository
import com.akash.expiryguard.ui.screens.addedit.AddEditItemScreen
import com.akash.expiryguard.ui.screens.detail.ItemDetailScreen
import com.akash.expiryguard.ui.screens.expenses.ExpenseInsightsScreen
import com.akash.expiryguard.ui.screens.expenses.ExpenseInsightsViewModel
import com.akash.expiryguard.ui.screens.home.HomeScreen
import com.akash.expiryguard.ui.screens.home.HomeViewModel
import com.akash.expiryguard.ui.screens.settings.SettingsScreen

@Composable
fun ExpiryGuardNavHost(
    navController: NavHostController,
    repository: ExpiryItemRepository
) {
    NavHost(
        navController = navController,
        startDestination = ExpiryGuardDestination.HOME
    ) {
        composable(ExpiryGuardDestination.HOME) {
            val viewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.Factory(repository)
            )
            HomeScreen(
                viewModel = viewModel,
                onAddItemClick = { navController.navigate(ExpiryGuardDestination.ADD_EDIT) },
                onItemClick = { itemId -> navController.navigate(ExpiryGuardDestination.detail(itemId)) },
                onSettingsClick = { navController.navigate(ExpiryGuardDestination.SETTINGS) },
                onExpenseInsightsClick = { navController.navigate(ExpiryGuardDestination.EXPENSE_INSIGHTS) }
            )
        }

        composable(ExpiryGuardDestination.EXPENSE_INSIGHTS) {
            val viewModel: ExpenseInsightsViewModel = viewModel(
                factory = ExpenseInsightsViewModel.Factory(repository)
            )
            ExpenseInsightsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(ExpiryGuardDestination.ADD_EDIT) {
            AddEditItemScreen()
        }

        composable(ExpiryGuardDestination.DETAIL_ROUTE) {
            ItemDetailScreen()
        }

        composable(ExpiryGuardDestination.SETTINGS) {
            SettingsScreen()
        }
    }
}
