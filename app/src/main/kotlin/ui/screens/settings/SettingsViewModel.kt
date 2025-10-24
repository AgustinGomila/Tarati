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

private data class SettingsFlows(
    val isDarkTheme: Boolean,
    val difficulty: Difficulty,
    val language: AppLanguage,
    val labelsVisibility: Boolean,
    val verticesVisibility: Boolean,
    val edgesVisibility: Boolean,
    val animateEffects: Boolean,
    val palette: String
)

class SettingsViewModel(val sr: SettingsRepository = get().get()) : ViewModel() {

    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            val flows = listOf(
                sr.isDarkTheme,
                sr.difficulty,
                sr.language,
                sr.labelsVisibility,
                sr.verticesVisibility,
                sr.edgesVisibility,
                sr.animateEffects,
                sr.palette
            )

            @Suppress("TYPE_INTERSECTION_AS_REIFIED_WARNING")
            combine(flows) { values ->
                SettingsFlows(
                    isDarkTheme = values[0] as Boolean,
                    difficulty = values[1] as Difficulty,
                    language = values[2] as AppLanguage,
                    labelsVisibility = values[3] as Boolean,
                    verticesVisibility = values[4] as Boolean,
                    edgesVisibility = values[5] as Boolean,
                    animateEffects = values[6] as Boolean,
                    palette = values[7] as String
                )
            }.collect { flows ->
                _settingsState.value = SettingsState(
                    appTheme = if (flows.isDarkTheme) AppTheme.MODE_NIGHT else AppTheme.MODE_AUTO,
                    difficulty = flows.difficulty,
                    language = flows.language,
                    boardState = BoardState(
                        labelsVisibles = flows.labelsVisibility,
                        verticesVisibles = flows.verticesVisibility,
                        edgesVisibles = flows.edgesVisibility,
                        animateEffects = flows.animateEffects,
                    ),
                    palette = flows.palette
                )
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

    fun setPalette(paletteName: String) {
        viewModelScope.launch {
            sr.setPalette(paletteName)
        }
    }

    fun setDifficulty(newDifficulty: Difficulty) {
        viewModelScope.launch {
            sr.setDifficulty(newDifficulty)
        }
    }

    fun setLabelsVisibility(visible: Boolean) {
        viewModelScope.launch {
            sr.setLabelsVisibility(visible)
        }
    }

    fun setVerticesVisibility(visible: Boolean) {
        viewModelScope.launch {
            sr.setVerticesVisibility(visible)
        }
    }

    fun setEdgesVisibility(visible: Boolean) {
        viewModelScope.launch {
            sr.setEdgesVisibility(visible)
        }
    }

    fun setAnimateEffects(animate: Boolean) {
        viewModelScope.launch {
            sr.setAnimateEffects(animate)
        }
    }
}