package com.akash.expiryguard.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LoginSignUpScreen(
    viewModel: AuthViewModel,
    onAuthenticated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "ExpiryGuard", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = if (uiState.mode == AuthMode.LOGIN) "Log in to continue" else "Create your account",
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(top = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.mode == AuthMode.LOGIN,
                onClick = { viewModel.setMode(AuthMode.LOGIN) },
                label = { Text(text = "Login") }
            )
            FilterChip(
                selected = uiState.mode == AuthMode.SIGN_UP,
                onClick = { viewModel.setMode(AuthMode.SIGN_UP) },
                label = { Text(text = "Sign up") }
            )
        }
        OutlinedTextField(
            value = uiState.username,
            onValueChange = viewModel::onUsernameChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            singleLine = true,
            label = { Text(text = "Username") }
        )
        OutlinedTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            singleLine = true,
            label = { Text(text = "Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                modifier = Modifier.padding(top = 12.dp),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Button(
            onClick = { viewModel.submit(onAuthenticated) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            enabled = !uiState.isSubmitting
        ) {
            Text(text = if (uiState.isSubmitting) "Please wait..." else if (uiState.mode == AuthMode.LOGIN) "Login" else "Sign up")
        }
    }
}
