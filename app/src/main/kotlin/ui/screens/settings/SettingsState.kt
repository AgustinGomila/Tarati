package com.agustin.tarati.ui.screens.settings

import com.agustin.tarati.game.ai.Difficulty
import com.agustin.tarati.ui.localization.AppLanguage
import com.agustin.tarati.ui.theme.AppTheme
import com.agustin.tarati.ui.theme.availablePalettes

data class BoardState(
    val labelsVisibles: Boolean = false,
    val verticesVisibles: Boolean = true,
    val edgesVisibles: Boolean = false,
    val animateEffects: Boolean = true,
)

data class SettingsState(
    val appTheme: AppTheme = AppTheme.MODE_AUTO,
    val difficulty: Difficulty = Difficulty.DEFAULT,
    val language: AppLanguage = AppLanguage.SPANISH,
    val tutorialButtonVisible: Boolean = true,
    val boardState: BoardState = BoardState(),
    val palette: String = availablePalettes.first().name
)