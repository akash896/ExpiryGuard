package com.akash.expiryguard.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akash.expiryguard.data.local.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val appPreferences: AppPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun nextPage() {
        _uiState.update { state ->
            state.copy(pageIndex = (state.pageIndex + 1).coerceAtMost(LAST_PAGE_INDEX))
        }
    }

    fun previousPage() {
        _uiState.update { state ->
            state.copy(pageIndex = (state.pageIndex - 1).coerceAtLeast(0))
        }
    }

    fun complete(onComplete: () -> Unit) {
        if (_uiState.value.isCompleting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCompleting = true, errorMessage = null) }
            try {
                appPreferences.completeOnboarding()
                onComplete()
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isCompleting = false,
                        errorMessage = error.message ?: "Unable to save onboarding progress."
                    )
                }
            }
        }
    }

    class Factory(
        private val appPreferences: AppPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingViewModel(appPreferences) as T
        }
    }

    private companion object {
        const val LAST_PAGE_INDEX = 2
    }
}

data class OnboardingUiState(
    val pageIndex: Int = 0,
    val isCompleting: Boolean = false,
    val errorMessage: String? = null
)
