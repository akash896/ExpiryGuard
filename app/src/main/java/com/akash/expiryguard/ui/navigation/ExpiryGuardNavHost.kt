package com.akash.expiryguard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.akash.expiryguard.data.repository.ExpiryItemRepository
import com.akash.expiryguard.ui.screens.addedit.AddEditItemScreen
import com.akash.expiryguard.ui.screens.addedit.AddEditItemViewModel
import com.akash.expiryguard.ui.screens.detail.ItemDetailScreen
import com.akash.expiryguard.ui.screens.detail.ItemDetailViewModel
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
                onAddItemClick = { navController.navigate(ExpiryGuardDestination.addEdit()) },
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

        composable(
            route = ExpiryGuardDestination.ADD_EDIT,
            arguments = listOf(
                navArgument("itemId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")
            val viewModel: AddEditItemViewModel = viewModel(
                factory = AddEditItemViewModel.Factory(repository, itemId)
            )
            AddEditItemScreen(
                viewModel = viewModel,
                isEditing = !itemId.isNullOrBlank(),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = ExpiryGuardDestination.DETAIL_ROUTE,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = requireNotNull(backStackEntry.arguments?.getString("itemId"))
            val viewModel: ItemDetailViewModel = viewModel(
                factory = ItemDetailViewModel.Factory(repository, itemId)
            )
            ItemDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onEditItem = { id -> navController.navigate(ExpiryGuardDestination.addEdit(id)) }
            )
        }

        composable(ExpiryGuardDestination.SETTINGS) {
            SettingsScreen()
        }
    }
}
