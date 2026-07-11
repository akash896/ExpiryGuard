package com.akash.expiryguard

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.akash.expiryguard.data.repository.ExpiryItemRepository
import com.akash.expiryguard.notifications.NotificationHelper
import com.akash.expiryguard.ui.ExpiryGuardApp
import com.akash.expiryguard.ui.theme.ExpiryGuardTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val authState = mutableStateOf<AuthBootstrapState>(AuthBootstrapState.Loading)
    private val useDarkTheme = mutableStateOf(false)
    private lateinit var repository: ExpiryItemRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = (application as ExpiryGuardApplication).container.itemRepository
        useDarkTheme.value = getSharedPreferences(THEME_PREFERENCES, MODE_PRIVATE)
            .getBoolean(DARK_THEME_KEY, false)
        requestNotificationPermissionIfNeeded()
        signInAnonymously()

        setContent {
            ExpiryGuardTheme(darkTheme = useDarkTheme.value, dynamicColor = false) {
                when (val state = authState.value) {
                    AuthBootstrapState.Loading -> BootstrapStatusScreen("Signing you in...")
                    AuthBootstrapState.Ready -> ExpiryGuardApp(
                        repository = repository,
                        useDarkTheme = useDarkTheme.value,
                        onThemeChange = ::setDarkTheme
                    )
                    is AuthBootstrapState.Error -> BootstrapStatusScreen(
                        message = state.message,
                        isError = true
                    )
                }
            }
        }
    }

    private fun signInAnonymously() {
        lifecycleScope.launch {
            authState.value = try {
                repository.signInAnonymouslyIfNeeded()
                AuthBootstrapState.Ready
            } catch (error: Exception) {
                AuthBootstrapState.Error(error.message ?: "Anonymous sign-in failed.")
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        if (!NotificationHelper.canPostNotifications(this)) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
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
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 42
        const val THEME_PREFERENCES = "app_theme"
        const val DARK_THEME_KEY = "dark_theme"
    }
}

private sealed interface AuthBootstrapState {
    data object Loading : AuthBootstrapState
    data object Ready : AuthBootstrapState
    data class Error(val message: String) : AuthBootstrapState
}

@Composable
private fun BootstrapStatusScreen(
    message: String,
    isError: Boolean = false
) {
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                color = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onBackground
                }
            )
        }
    }
}
