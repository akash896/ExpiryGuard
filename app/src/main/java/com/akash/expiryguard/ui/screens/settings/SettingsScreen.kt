package com.akash.expiryguard.ui.screens.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.akash.expiryguard.data.model.CategoryReminderDefaults
import com.akash.expiryguard.data.model.NotificationSettings
import com.akash.expiryguard.notifications.ExpiryReminderScheduler
import com.akash.expiryguard.notifications.NotificationHelper
import kotlinx.coroutines.launch
import java.util.Locale

private val ReminderDayOptions = listOf(0, 1, 3, 7, 15, 30)

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    useDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showArchiveConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        val message = uiState.errorMessage ?: uiState.successMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(uiState.notificationSettings) {
        ExpiryReminderScheduler.scheduleDaily(context, uiState.notificationSettings)
    }

    SettingsContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        useDarkTheme = useDarkTheme,
        onThemeChange = onThemeChange,
        onNavigateBack = onNavigateBack,
        onReminderTimeChange = viewModel::updateReminderCheckTime,
        onCategoryReminderDaysChange = viewModel::updateCategoryReminderDays,
        onRunReminderCheck = {
            ExpiryReminderScheduler.enqueueReminderCheck(context)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Expiry check scheduled.")
            }
        },
        onSendTestNotification = {
            val message = if (NotificationHelper.showTestNotification(context)) {
                "Test notification sent."
            } else {
                "Notifications are not allowed."
            }
            coroutineScope.launch { snackbarHostState.showSnackbar(message) }
        },
        onArchiveExpiredItems = { showArchiveConfirmation = true }
    )

    if (showArchiveConfirmation) {
        AlertDialog(
            onDismissRequest = { showArchiveConfirmation = false },
            title = { Text(text = "Archive old expired items?") },
            text = { Text(text = "Items expired more than 30 days ago will be archived.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showArchiveConfirmation = false
                        viewModel.archiveExpiredItemsOlderThan30Days()
                    }
                ) {
                    Text(text = "Archive")
                }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveConfirmation = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    snackbarHostState: SnackbarHostState,
    useDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onReminderTimeChange: (Int, Int) -> Unit,
    onCategoryReminderDaysChange: (String, Int) -> Unit,
    onRunReminderCheck: () -> Unit,
    onSendTestNotification: () -> Unit,
    onArchiveExpiredItems: () -> Unit
) {
    val context = LocalContext.current
    val notificationsAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    var showTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Settings") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) { Text(text = "Back") }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "ExpiryGuard", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        text = "Version 1.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item { HorizontalDivider() }
            item { SettingRow(label = "Signed-in user", value = "Anonymous user") }
            item {
                SettingRow(
                    label = "Notifications",
                    value = if (notificationsAllowed) "Allowed" else "Not allowed"
                )
            }
            item {
                SettingRow(
                    label = "Data storage",
                    value = "Firebase Firestore under your current user account"
                )
            }
            item {
                SettingRow(
                    label = "Price data",
                    value = "Used only for in-app spending and expiry insights"
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Dark theme", style = MaterialTheme.typography.labelLarge)
                    Switch(checked = useDarkTheme, onCheckedChange = onThemeChange)
                }
            }
            item { HorizontalDivider() }
            item {
                Text(text = "Reminder settings", style = MaterialTheme.typography.titleMedium)
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Reminder check time", style = MaterialTheme.typography.labelLarge)
                        Text(
                            text = "Best effort; Android may defer background work.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(onClick = { showTimePicker = true }) {
                        Text(text = formatTime(uiState.notificationSettings))
                    }
                }
            }
            item {
                Text(
                    text = "Category default reminder",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            items(CategoryReminderDefaults.categories, key = { it }) { category ->
                CategoryReminderDaysRow(
                    category = category,
                    reminderDays = uiState.notificationSettings.reminderDaysFor(category),
                    onReminderDaysChange = { days ->
                        onCategoryReminderDaysChange(category, days)
                    }
                )
            }
            item { HorizontalDivider() }
            item {
                Button(
                    onClick = onRunReminderCheck,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Run expiry check now")
                }
            }
            item {
                OutlinedButton(
                    onClick = onSendTestNotification,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Send test notification")
                }
            }
            item {
                OutlinedButton(
                    onClick = onArchiveExpiredItems,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isArchiving
                ) {
                    if (uiState.isArchiving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    Text(text = if (uiState.isArchiving) "Archiving..." else "Archive expired items older than 30 days")
                }
            }
        }
    }

    if (showTimePicker) {
        ReminderTimePickerDialog(
            initialHour = uiState.notificationSettings.reminderCheckHour,
            initialMinute = uiState.notificationSettings.reminderCheckMinute,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                onReminderTimeChange(hour, minute)
                showTimePicker = false
            }
        )
    }
}

@Composable
private fun CategoryReminderDaysRow(
    category: String,
    reminderDays: Int,
    onReminderDaysChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = category, style = MaterialTheme.typography.bodyLarge)
        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(text = reminderLabel(reminderDays))
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                ReminderDayOptions.forEach { days ->
                    DropdownMenuItem(
                        text = { Text(text = reminderLabel(days)) },
                        onClick = {
                            onReminderDaysChange(days)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Reminder check time") },
        text = { TimePicker(state = timePickerState) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(timePickerState.hour, timePickerState.minute)
                }
            ) {
                Text(text = "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}

@Composable
private fun SettingRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatTime(settings: NotificationSettings): String {
    return String.format(
        Locale.US,
        "%02d:%02d",
        settings.reminderCheckHour,
        settings.reminderCheckMinute
    )
}

private fun reminderLabel(days: Int): String {
    return if (days == 1) "1 day" else "$days days"
}
