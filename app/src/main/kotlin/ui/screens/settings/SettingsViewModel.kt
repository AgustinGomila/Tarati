package com.agustin.tarati.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agustin.tarati.ui.localization.AppLanguage
import com.agustin.tarati.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext.get

class SettingsViewModel() : ViewModel() {

    val sr: SettingsRepository by lazy { get().get() }

    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                sr.isDarkTheme,
                sr.difficulty,
                sr.language
            ) { isDark, difficulty, language ->
                SettingsState(
                    appTheme = if (isDark) AppTheme.MODE_NIGHT else AppTheme.MODE_AUTO,
                    difficulty = difficulty,
                    language = language
                )
            }.collect { newState ->
                _settingsState.value = newState
            }
        }
    }

    fun toggleDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            sr.setDarkTheme(enabled)
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            sr.setLanguage(language)
        }
    }
}