package com.agustin.tarati.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agustin.tarati.game.ai.Difficulty
import com.agustin.tarati.ui.localization.AppLanguage
import com.agustin.tarati.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext.get

class SettingsViewModel() : ViewModel() {
    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()
    val repository: SettingsRepository = get().get()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                repository.isDarkTheme,
                repository.difficulty,
                repository.language
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
            repository.setDarkTheme(enabled)
        }
    }

    fun setDifficulty(difficulty: Difficulty) {
        viewModelScope.launch {
            repository.setDifficulty(difficulty)
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            repository.setLanguage(language)
        }
    }
}