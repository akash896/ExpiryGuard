package com.akash.expiryguard.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.akash.expiryguard.data.local.AppPreferences
import com.akash.expiryguard.data.repository.ExpiryItemRepository
import com.akash.expiryguard.ui.navigation.AppNavGraph
import com.akash.expiryguard.ui.navigation.AppRoutes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ExpiryGuardApp(
    repository: ExpiryItemRepository,
    appPreferences: AppPreferences,
    useDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val onboardingCompleted by appPreferences.onboardingCompleted.collectAsStateWithLifecycle(
        initialValue = null
    )

    if (onboardingCompleted == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val navController = rememberNavController()
    AppNavGraph(
        navController = navController,
        repository = repository,
        appPreferences = appPreferences,
        startDestination = if (onboardingCompleted == true) AppRoutes.HOME else AppRoutes.ONBOARDING,
        useDarkTheme = useDarkTheme,
        onThemeChange = onThemeChange
    )
}
