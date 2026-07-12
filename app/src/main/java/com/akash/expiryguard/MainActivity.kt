package com.akash.expiryguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.akash.expiryguard.data.repository.ExpiryItemRepository
import com.akash.expiryguard.ui.ExpiryGuardApp
import com.akash.expiryguard.ui.theme.ExpiryGuardTheme

class MainActivity : ComponentActivity() {
    private val useDarkTheme = mutableStateOf(false)
    private lateinit var repository: ExpiryItemRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = (application as ExpiryGuardApplication).container.itemRepository
        (application as ExpiryGuardApplication).container.authRepository.signOutAnonymousUserIfPresent()
        useDarkTheme.value = getSharedPreferences(THEME_PREFERENCES, MODE_PRIVATE)
            .getBoolean(DARK_THEME_KEY, false)
        setContent {
            ExpiryGuardTheme(darkTheme = useDarkTheme.value, dynamicColor = false) {
                ExpiryGuardApp(
                    repository = repository,
                    shoppingListRepository = (application as ExpiryGuardApplication)
                        .container
                        .shoppingListRepository,
                    authRepository = (application as ExpiryGuardApplication).container.authRepository,
                    appPreferences = (application as ExpiryGuardApplication).container.appPreferences,
                    useDarkTheme = useDarkTheme.value,
                    onThemeChange = ::setDarkTheme
                )
            }
        }
    }

    private fun setDarkTheme(enabled: Boolean) {
        useDarkTheme.value = enabled
        getSharedPreferences(THEME_PREFERENCES, MODE_PRIVATE)
            .edit()
            .putBoolean(DARK_THEME_KEY, enabled)
            .apply()
    }

    private companion object {
        const val THEME_PREFERENCES = "app_theme"
        const val DARK_THEME_KEY = "dark_theme"
    }
}
