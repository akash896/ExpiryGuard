package com.akash.expiryguard.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akash.expiryguard.data.auth.FirebaseAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: FirebaseAuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun setMode(mode: AuthMode) {
        _uiState.update { it.copy(mode = mode, errorMessage = null) }
    }

    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun submit(onAuthenticated: () -> Unit) {
        val state = _uiState.value
        if (state.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            try {
                if (state.mode == AuthMode.SIGN_UP) {
                    authRepository.signUp(state.username, state.password)
                } else {
                    authRepository.logIn(state.username, state.password)
                }
                _uiState.update { it.copy(isSubmitting = false) }
                onAuthenticated()
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = error.message ?: "Unable to authenticate. Check your username and password."
                    )
                }
            }
        }
    }

    class Factory(
        private val authRepository: FirebaseAuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(authRepository) as T
        }
    }
}

enum class AuthMode {
    LOGIN,
    SIGN_UP
}

data class AuthUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val username: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)
