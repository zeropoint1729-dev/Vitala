package com.faridul.vitala.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "vitala_prefs")

class PreferencesManager(private val context: Context) {

    val hasAcceptedDisclaimer: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[HAS_ACCEPTED_DISCLAIMER] ?: false
    }

    suspend fun setAcceptedDisclaimer() {
        context.dataStore.edit { prefs -> prefs[HAS_ACCEPTED_DISCLAIMER] = true }
    }

    val reminderHour: Flow<Int> = context.dataStore.data.map { prefs -> prefs[REMINDER_HOUR] ?: 8 }
    val reminderMinute: Flow<Int> = context.dataStore.data.map { prefs -> prefs[REMINDER_MINUTE] ?: 0 }
    val reminderEnabled: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[REMINDER_ENABLED] ?: false }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[REMINDER_HOUR] = hour
            prefs[REMINDER_MINUTE] = minute
            prefs[REMINDER_ENABLED] = true
        }
    }

    private companion object {
        val HAS_ACCEPTED_DISCLAIMER = booleanPreferencesKey("has_accepted_disclaimer")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
    }
}
