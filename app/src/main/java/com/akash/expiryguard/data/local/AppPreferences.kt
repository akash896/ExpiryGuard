package com.akash.expiryguard.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.akash.expiryguard.data.model.CategoryReminderDefaults
import com.akash.expiryguard.data.model.NotificationSettings
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

    val notificationSettings: Flow<NotificationSettings> = context.appPreferencesDataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            NotificationSettings(
                reminderCheckHour = (preferences[REMINDER_CHECK_HOUR]
                    ?: NotificationSettings.DEFAULT_REMINDER_CHECK_HOUR).coerceIn(0, 23),
                reminderCheckMinute = (preferences[REMINDER_CHECK_MINUTE]
                    ?: NotificationSettings.DEFAULT_REMINDER_CHECK_MINUTE).coerceIn(0, 59),
                categoryReminderDays = CategoryReminderDefaults.defaults.mapValues { (category, defaultDays) ->
                    preferences[categoryReminderDaysKey(category)]?.coerceAtLeast(0) ?: defaultDays
                }
            )
        }

    suspend fun completeOnboarding() {
        context.appPreferencesDataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = true
        }
    }

    suspend fun updateReminderCheckTime(hour: Int, minute: Int) {
        context.appPreferencesDataStore.edit { preferences ->
            preferences[REMINDER_CHECK_HOUR] = hour.coerceIn(0, 23)
            preferences[REMINDER_CHECK_MINUTE] = minute.coerceIn(0, 59)
        }
    }

    suspend fun updateCategoryReminderDays(category: String, reminderDays: Int) {
        if (category !in CategoryReminderDefaults.categories) return

        context.appPreferencesDataStore.edit { preferences ->
            preferences[categoryReminderDaysKey(category)] = reminderDays.coerceAtLeast(0)
        }
    }

    private fun categoryReminderDaysKey(category: String) =
        intPreferencesKey("category_reminder_days_${category.lowercase()}")

    private companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val REMINDER_CHECK_HOUR = intPreferencesKey("reminder_check_hour")
        val REMINDER_CHECK_MINUTE = intPreferencesKey("reminder_check_minute")
    }
}
