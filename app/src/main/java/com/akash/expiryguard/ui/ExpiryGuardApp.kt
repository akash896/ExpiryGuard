package com.akash.expiryguard.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.akash.expiryguard.data.ExpiryItemRepository
import com.akash.expiryguard.ui.navigation.ExpiryGuardNavHost

@Composable
fun ExpiryGuardApp(repository: ExpiryItemRepository) {
    val navController = rememberNavController()
    ExpiryGuardNavHost(
        navController = navController,
        repository = repository
    )
}
