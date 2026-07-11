package com.akash.expiryguard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.akash.expiryguard.data.local.AppPreferences
import com.akash.expiryguard.data.repository.ExpiryItemRepository
import com.akash.expiryguard.ui.screens.addedit.AddEditItemScreen
import com.akash.expiryguard.ui.screens.addedit.AddEditItemViewModel
import com.akash.expiryguard.ui.screens.detail.ItemDetailScreen
import com.akash.expiryguard.ui.screens.detail.ItemDetailViewModel
import com.akash.expiryguard.ui.screens.expenses.ExpenseInsightsScreen
import com.akash.expiryguard.ui.screens.expenses.ExpenseInsightsViewModel
import com.akash.expiryguard.ui.screens.home.HomeScreen
import com.akash.expiryguard.ui.screens.home.HomeViewModel
import com.akash.expiryguard.ui.screens.onboarding.OnboardingScreen
import com.akash.expiryguard.ui.screens.onboarding.OnboardingViewModel
import com.akash.expiryguard.ui.screens.settings.SettingsScreen
import com.akash.expiryguard.ui.screens.settings.SettingsViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    repository: ExpiryItemRepository,
    appPreferences: AppPreferences,
    startDestination: String,
    useDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppRoutes.ONBOARDING) {
            val viewModel: OnboardingViewModel = viewModel(
                factory = OnboardingViewModel.Factory(appPreferences)
            )
            OnboardingScreen(
                viewModel = viewModel,
                onComplete = {
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.ONBOARDING) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(AppRoutes.HOME) {
            val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(repository))
            HomeScreen(
                viewModel = viewModel,
                onAddItemClick = { navController.navigate(AppRoutes.ADD_ITEM) },
                onItemClick = { itemId -> navController.navigate(AppRoutes.detail(itemId)) },
                onSettingsClick = { navController.navigate(AppRoutes.SETTINGS) },
                onExpenseInsightsClick = { navController.navigate(AppRoutes.EXPENSE_INSIGHTS) }
            )
        }

        composable(AppRoutes.ADD_ITEM) {
            AddEditDestination(
                repository = repository,
                itemId = null,
                onNavigateBack = navController::popBackStack
            )
        }

        composable(
            route = AppRoutes.EDIT_ITEM,
            arguments = listOf(navArgument(AppRoutes.ITEM_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = requireNotNull(backStackEntry.arguments?.getString(AppRoutes.ITEM_ID))
            AddEditDestination(
                repository = repository,
                itemId = itemId,
                onNavigateBack = navController::popBackStack
            )
        }

        composable(
            route = AppRoutes.DETAIL,
            arguments = listOf(navArgument(AppRoutes.ITEM_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = requireNotNull(backStackEntry.arguments?.getString(AppRoutes.ITEM_ID))
            val viewModel: ItemDetailViewModel = viewModel(
                factory = ItemDetailViewModel.Factory(repository, itemId)
            )
            ItemDetailScreen(
                viewModel = viewModel,
                onNavigateBack = navController::popBackStack,
                onEditItem = { id -> navController.navigate(AppRoutes.editItem(id)) }
            )
        }

        composable(AppRoutes.SETTINGS) {
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(repository)
            )
            SettingsScreen(
                viewModel = viewModel,
                useDarkTheme = useDarkTheme,
                onThemeChange = onThemeChange,
                onNavigateBack = navController::popBackStack
            )
        }

        composable(AppRoutes.EXPENSE_INSIGHTS) {
            val viewModel: ExpenseInsightsViewModel = viewModel(
                factory = ExpenseInsightsViewModel.Factory(repository)
            )
            ExpenseInsightsScreen(
                viewModel = viewModel,
                onNavigateBack = navController::popBackStack
            )
        }
    }
}

@Composable
private fun AddEditDestination(
    repository: ExpiryItemRepository,
    itemId: String?,
    onNavigateBack: () -> Unit
) {
    val viewModel: AddEditItemViewModel = viewModel(
        factory = AddEditItemViewModel.Factory(repository, itemId)
    )
    AddEditItemScreen(
        viewModel = viewModel,
        isEditing = itemId != null,
        onNavigateBack = onNavigateBack
    )
}
