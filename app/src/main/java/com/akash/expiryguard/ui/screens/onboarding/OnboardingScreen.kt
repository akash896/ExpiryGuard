package com.akash.expiryguard.ui.screens.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.akash.expiryguard.notifications.NotificationHelper

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    OnboardingContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNext = viewModel::nextPage,
        onBack = viewModel::previousPage,
        onComplete = { viewModel.complete(onComplete) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OnboardingContent(
    uiState: OnboardingUiState,
    snackbarHostState: SnackbarHostState,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onComplete: () -> Unit
) {
    val page = ONBOARDING_PAGES[uiState.pageIndex]
    val context = LocalContext.current
    var notificationsAllowed by remember {
        mutableStateOf(NotificationHelper.canPostNotifications(context))
    }
    var notificationRequestResult by remember { mutableStateOf<Boolean?>(null) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsAllowed = granted
        notificationRequestResult = granted
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "ExpiryGuard") },
                actions = {
                    TextButton(onClick = onComplete, enabled = !uiState.isCompleting) {
                        Text(text = "Skip")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "${uiState.pageIndex + 1} of ${ONBOARDING_PAGES.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                LinearProgressIndicator(
                    progress = { (uiState.pageIndex + 1).toFloat() / ONBOARDING_PAGES.size },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = page.title,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = page.description,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                if (uiState.pageIndex == LAST_PAGE_INDEX) {
                    NotificationPermissionEducation(
                        notificationsAllowed = notificationsAllowed,
                        notificationRequestResult = notificationRequestResult,
                        onRequestPermission = {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.pageIndex > 0) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isCompleting
                    ) {
                        Text(text = "Back")
                    }
                }
                Button(
                    onClick = if (uiState.pageIndex == LAST_PAGE_INDEX) onComplete else onNext,
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isCompleting
                ) {
                    if (uiState.isCompleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(18.dp).width(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(text = if (uiState.pageIndex == LAST_PAGE_INDEX) "Get Started" else "Next")
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationPermissionEducation(
    notificationsAllowed: Boolean,
    notificationRequestResult: Boolean?,
    onRequestPermission: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = when {
                notificationsAllowed -> "Notifications are enabled."
                notificationRequestResult == false -> "Notifications are off. You can still continue and enable them later in Settings."
                else -> "Enable notifications to receive timely reminders."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationsAllowed) {
            OutlinedButton(onClick = onRequestPermission) {
                Text(text = "Enable notifications")
            }
        }
    }
}

private data class OnboardingPage(
    val title: String,
    val description: String
)

private val ONBOARDING_PAGES = listOf(
    OnboardingPage(
        title = "Track Before It Expires",
        description = "Add food, medicines, documents, warranties, and subscriptions before they expire."
    ),
    OnboardingPage(
        title = "See Your Wasted Value",
        description = "Add prices to understand spending, consumed value, active value, and expired value."
    ),
    OnboardingPage(
        title = "Get Timely Reminders",
        description = "Enable notifications so ExpiryGuard can remind you before items expire."
    )
)

private const val LAST_PAGE_INDEX = 2
