package com.akash.expiryguard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.akash.expiryguard.ui.screens.addedit.AddEditItemScreen
import com.akash.expiryguard.ui.screens.detail.ItemDetailScreen
import com.akash.expiryguard.ui.screens.home.HomeScreen
import com.akash.expiryguard.ui.screens.settings.SettingsScreen

@Composable
fun ExpiryGuardNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = ExpiryGuardDestination.HOME
    ) {
        composable(ExpiryGuardDestination.HOME) {
            HomeScreen()
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
