package com.agustin.tarati.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf

@Stable
object PaletteManager {
    private val _currentPalette = mutableStateOf<BoardPalette>(ClassicPalette)
    val currentPalette: BoardPalette get() = _currentPalette.value

    fun setPalette(palette: BoardPalette) {
        _currentPalette.value = palette
    }

    fun getPaletteByName(name: String): BoardPalette? {
        return availablePalettes.find { it.name == name }
    }
}

fun changePalette(paletteName: String) {
    val palette = PaletteManager.getPaletteByName(paletteName)
    palette?.let { PaletteManager.setPalette(it) }
}

@Composable
fun rememberBoardColors(): BoardColors {
    val palette = PaletteManager.currentPalette
    return BoardColors(
        neutralColor = palette.neutralColor,
        vertexOccupiedColor = palette.vertexOccupiedColor,
        vertexSelectedColor = palette.vertexSelectedColor,
        vertexHighlightColor = palette.vertexHighlightColor,
        textColor = palette.textColor,
        blackPieceColor = palette.blackPieceColor,
        whitePieceColor = palette.whitePieceColor,
        blackPieceBorderColor = palette.blackPieceBorderColor,
        whitePieceBorderColor = palette.whitePieceBorderColor,
        selectionIndicatorColor = palette.selectionIndicatorColor,
        boardVertexColor = palette.boardVertexColor,
        boardEdgeColor = palette.boardEdgeColor,
        boardBackground = palette.boardBackground,
        boardPatternColor1 = palette.boardPatternColor1,
        boardPatternColor2 = palette.boardPatternColor2,
        boardPatternColor3 = palette.boardPatternColor3,
        boardPatternBorderColor = palette.boardPatternBorderColor,
        boardGlowColor = palette.boardGlowColor,
    )
}