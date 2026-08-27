package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("calculator_settings", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadTheme())
    val themeMode: StateFlow<AppTheme> = _themeMode.asStateFlow()

    private val _hapticEnabled = MutableStateFlow(prefs.getBoolean(KEY_HAPTIC, true))
    val hapticEnabled: StateFlow<Boolean> = _hapticEnabled.asStateFlow()

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean(KEY_SOUND, false))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _thousandsSeparatorEnabled = MutableStateFlow(prefs.getBoolean(KEY_THOUSANDS_SEP, true))
    val thousandsSeparatorEnabled: StateFlow<Boolean> = _thousandsSeparatorEnabled.asStateFlow()

    fun setTheme(theme: AppTheme) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
        _themeMode.value = theme
    }

    fun setHapticEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply()
        _hapticEnabled.value = enabled
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply()
        _soundEnabled.value = enabled
    }

    fun setThousandsSeparatorEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_THOUSANDS_SEP, enabled).apply()
        _thousandsSeparatorEnabled.value = enabled
    }

    private fun loadTheme(): AppTheme {
        val name = prefs.getString(KEY_THEME, AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name
        return try {
            AppTheme.valueOf(name)
        } catch (e: Exception) {
            AppTheme.SYSTEM
        }
    }

    companion object {
        private const val KEY_THEME = "pref_theme"
        private const val KEY_HAPTIC = "pref_haptic"
        private const val KEY_SOUND = "pref_sound"
        private const val KEY_THOUSANDS_SEP = "pref_thousands_sep"
    }
}
