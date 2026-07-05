package com.akash.expiryguard.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.akash.expiryguard.ui.navigation.ExpiryGuardNavHost

@Composable
fun ExpiryGuardApp() {
    val navController = rememberNavController()
    ExpiryGuardNavHost(navController = navController)
}
