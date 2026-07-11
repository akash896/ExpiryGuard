package com.akash.expiryguard.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.akash.expiryguard.data.repository.ExpiryItemRepository
import com.akash.expiryguard.ui.navigation.AppNavGraph

@Composable
fun ExpiryGuardApp(repository: ExpiryItemRepository) {
    val navController = rememberNavController()
    AppNavGraph(
        navController = navController,
        repository = repository
    )
}
