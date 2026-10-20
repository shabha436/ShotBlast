package com.example.ui.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages Dark Mode and Light Mode app theme state and persistence.
 */
object ThemeManager {

    private const val PREFS_NAME = "shotblast_theme_prefs"
    private const val KEY_DARK_THEME = "is_dark_theme"

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun init(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val saved = prefs.getBoolean(KEY_DARK_THEME, true)
            _isDarkTheme.value = saved
        } catch (e: Exception) {
            _isDarkTheme.value = true
        }
    }

    fun setDarkTheme(isDark: Boolean, context: Context? = null) {
        _isDarkTheme.value = isDark
        if (context != null) {
            try {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().putBoolean(KEY_DARK_THEME, isDark).apply()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun toggleTheme(context: Context? = null) {
        setDarkTheme(!_isDarkTheme.value, context)
    }
}
