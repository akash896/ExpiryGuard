package com.akash.expiryguard.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.appPreferencesDataStore by preferencesDataStore(name = "app_preferences")

class AppPreferences(private val context: Context) {
    val onboardingCompleted: Flow<Boolean> = context.appPreferencesDataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences -> preferences[ONBOARDING_COMPLETED] ?: false }

    suspend fun completeOnboarding() {
        context.appPreferencesDataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = true
        }
    }

    private companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }
}
