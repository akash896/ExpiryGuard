package com.akash.expiryguard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.akash.expiryguard.data.ExpiryItemRepository
import com.akash.expiryguard.ui.addedit.AddEditItemScreen
import com.akash.expiryguard.ui.detail.ItemDetailScreen
import com.akash.expiryguard.ui.home.HomeScreen
import com.akash.expiryguard.ui.home.HomeViewModel
import com.akash.expiryguard.ui.settings.SettingsScreen

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
                onItemClick = { itemId ->
                    navController.navigate(ExpiryGuardDestination.detail(itemId))
                },
                onSettingsClick = { navController.navigate(ExpiryGuardDestination.SETTINGS) }
            )
        }

        composable(ExpiryGuardDestination.ADD_EDIT) {
            AddEditItemScreen(onNavigateBack = navController::popBackStack)
        }

        composable(ExpiryGuardDestination.DETAIL_ROUTE) { backStackEntry ->
            ItemDetailScreen(
                itemId = backStackEntry.arguments?.getString("itemId").orEmpty(),
                onNavigateBack = navController::popBackStack,
                onEditClick = { navController.navigate(ExpiryGuardDestination.ADD_EDIT) }
            )
        }

        composable(ExpiryGuardDestination.SETTINGS) {
            SettingsScreen(onNavigateBack = navController::popBackStack)
        }
    }
}
