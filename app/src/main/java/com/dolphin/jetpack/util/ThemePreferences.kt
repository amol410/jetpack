package com.dolphin.jetpack.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

enum class ThemeMode(val value: Int) {
    SYSTEM(0),
    LIGHT(1),
    DARK(2);

    companion object {
        fun fromValue(value: Int): ThemeMode {
            return values().find { it.value == value } ?: SYSTEM
        }
    }
}

class ThemePreferences(private val context: Context) {

    companion object {
        private val THEME_MODE_KEY = intPreferencesKey("theme_mode")
    }

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        ThemeMode.fromValue(preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.value)
    }

    // For backward compatibility with existing code
    val isDarkModeFlow: Flow<Boolean> = themeModeFlow.map { themeMode ->
        themeMode == ThemeMode.DARK
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode.value
        }
    }

    // For backward compatibility
    suspend fun setDarkMode(isDarkMode: Boolean) {
        setThemeMode(if (isDarkMode) ThemeMode.DARK else ThemeMode.LIGHT)
    }
}
