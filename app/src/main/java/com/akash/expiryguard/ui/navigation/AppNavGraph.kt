package com.akash.expiryguard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.akash.expiryguard.data.local.AppPreferences
import com.akash.expiryguard.data.auth.FirebaseAuthRepository
import com.akash.expiryguard.data.model.QuickAddTemplates
import com.akash.expiryguard.data.repository.ExpiryItemRepository
import com.akash.expiryguard.data.repository.ShoppingListRepository
import com.akash.expiryguard.ui.screens.addedit.AddEditItemScreen
import com.akash.expiryguard.ui.screens.addedit.AddEditItemViewModel
import com.akash.expiryguard.ui.screens.auth.AuthViewModel
import com.akash.expiryguard.ui.screens.auth.LoginSignUpScreen
import com.akash.expiryguard.ui.screens.calendar.CalendarScreen
import com.akash.expiryguard.ui.screens.calendar.CalendarViewModel
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
import com.akash.expiryguard.ui.screens.shopping.ShoppingListScreen
import com.akash.expiryguard.ui.screens.shopping.ShoppingListViewModel
import com.akash.expiryguard.ui.screens.reorder.HomeOrderScreen
import com.akash.expiryguard.ui.screens.reorder.HomeOrderType
import com.akash.expiryguard.ui.screens.reorder.HomeOrderViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    repository: ExpiryItemRepository,
    shoppingListRepository: ShoppingListRepository,
    authRepository: FirebaseAuthRepository,
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
                    navController.navigate(AppRoutes.AUTHENTICATION) {
                        popUpTo(AppRoutes.ONBOARDING) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(AppRoutes.AUTHENTICATION) {
            val viewModel: AuthViewModel = viewModel(
                factory = AuthViewModel.Factory(authRepository)
            )
            LoginSignUpScreen(
                viewModel = viewModel,
                onAuthenticated = {
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.AUTHENTICATION) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(AppRoutes.HOME) {
            val viewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.Factory(repository, appPreferences)
            )
            HomeScreen(
                viewModel = viewModel,
                onAddItemClick = { navController.navigate(AppRoutes.ADD_ITEM) },
                onQuickAddClick = { template ->
                    navController.navigate(AppRoutes.addItem(template.templateId))
                },
                onItemClick = { itemId -> navController.navigate(AppRoutes.detail(itemId)) },
                onShoppingListClick = { navController.navigate(AppRoutes.SHOPPING_LIST) },
                onCalendarClick = { navController.navigate(AppRoutes.CALENDAR) },
                onSettingsClick = { navController.navigate(AppRoutes.SETTINGS) },
                onExpenseInsightsClick = { navController.navigate(AppRoutes.EXPENSE_INSIGHTS) },
                onReorderQuickAddClick = { navController.navigate(AppRoutes.QUICK_ADD_ORDER) },
                onReorderCategoriesClick = { navController.navigate(AppRoutes.CATEGORY_ORDER) }
            )
        }

        composable(AppRoutes.QUICK_ADD_ORDER) {
            HomeOrderDestination(
                appPreferences = appPreferences,
                orderType = HomeOrderType.QUICK_ADD,
                onNavigateBack = navController::popBackStack
            )
        }

        composable(AppRoutes.CATEGORY_ORDER) {
            HomeOrderDestination(
                appPreferences = appPreferences,
                orderType = HomeOrderType.CATEGORIES,
                onNavigateBack = navController::popBackStack
            )
        }

        composable(
            route = AppRoutes.ADD_ITEM_WITH_TEMPLATE,
            arguments = listOf(
                navArgument(AppRoutes.TEMPLATE_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            AddEditDestination(
                repository = repository,
                appPreferences = appPreferences,
                itemId = null,
                templateId = backStackEntry.arguments?.getString(AppRoutes.TEMPLATE_ID),
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
                appPreferences = appPreferences,
                itemId = itemId,
                templateId = null,
                onNavigateBack = navController::popBackStack
            )
        }

        composable(
            route = AppRoutes.DETAIL,
            arguments = listOf(navArgument(AppRoutes.ITEM_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = requireNotNull(backStackEntry.arguments?.getString(AppRoutes.ITEM_ID))
            val viewModel: ItemDetailViewModel = viewModel(
                factory = ItemDetailViewModel.Factory(repository, shoppingListRepository, itemId)
            )
            ItemDetailScreen(
                viewModel = viewModel,
                onNavigateBack = navController::popBackStack,
                onEditItem = { id -> navController.navigate(AppRoutes.editItem(id)) },
                onAddToShoppingList = viewModel::addToShoppingList
            )
        }

        composable(AppRoutes.SHOPPING_LIST) {
            val viewModel: ShoppingListViewModel = viewModel(
                factory = ShoppingListViewModel.Factory(shoppingListRepository)
            )
            ShoppingListScreen(
                viewModel = viewModel,
                onNavigateBack = navController::popBackStack
            )
        }

        composable(AppRoutes.SETTINGS) {
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(repository, appPreferences)
            )
            SettingsScreen(
                viewModel = viewModel,
                useDarkTheme = useDarkTheme,
                onThemeChange = onThemeChange,
                onNavigateBack = navController::popBackStack
            )
        }

        composable(AppRoutes.CALENDAR) {
            val viewModel: CalendarViewModel = viewModel(
                factory = CalendarViewModel.Factory(repository)
            )
            CalendarScreen(
                viewModel = viewModel,
                onNavigateBack = navController::popBackStack,
                onItemClick = { itemId -> navController.navigate(AppRoutes.detail(itemId)) }
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
private fun HomeOrderDestination(
    appPreferences: AppPreferences,
    orderType: HomeOrderType,
    onNavigateBack: () -> Unit
) {
    val viewModel: HomeOrderViewModel = viewModel(
        factory = HomeOrderViewModel.Factory(appPreferences, orderType)
    )
    HomeOrderScreen(viewModel = viewModel, onNavigateBack = onNavigateBack)
}

@Composable
private fun AddEditDestination(
    repository: ExpiryItemRepository,
    appPreferences: AppPreferences,
    itemId: String?,
    templateId: String?,
    onNavigateBack: () -> Unit
) {
    val viewModel: AddEditItemViewModel = viewModel(
        factory = AddEditItemViewModel.Factory(
            repository = repository,
            itemId = itemId,
            appPreferences = appPreferences,
            template = QuickAddTemplates.find(templateId)
        )
    )
    AddEditItemScreen(
        viewModel = viewModel,
        isEditing = itemId != null,
        onNavigateBack = onNavigateBack
    )
}
