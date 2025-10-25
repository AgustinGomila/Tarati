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
}

@Composable
fun rememberBoardColors(): BoardColors {
    val palette = PaletteManager.currentPalette
    return BoardColors(
        blackCobBorderColor = palette.blackCobBorderColor,
        blackCobColor = palette.blackCobColor,
        boardBackground = palette.boardBackground,
        boardEdgeColor = palette.boardEdgeColor,
        boardPerimeterColor = palette.boardPerimeterColor,
        boardPatternBorderColor = palette.boardPatternBorderColor,
        boardPatternColor1 = palette.boardPatternColor1,
        boardPatternColor2 = palette.boardPatternColor2,
        boardPatternColor3 = palette.boardPatternColor3,
        boardVertexColor = palette.boardVertexColor,
        neutralColor = palette.neutralColor,
        selectionIndicatorColor = palette.selectionIndicatorColor,
        textColor = palette.textColor,
        vertexAdjacentColor = palette.vertexAdjacentColor,
        vertexOccupiedColor = palette.vertexOccupiedColor,
        vertexSelectedColor = palette.vertexSelectedColor,
        whiteCobBorderColor = palette.whiteCobBorderColor,
        whiteCobColor = palette.whiteCobColor,

        highlightEdge1Color = palette.highlightEdge1Color,
        highlightEdge2Color = palette.highlightEdge2Color,
        highlightEdge3Color = palette.highlightEdge3Color,
        highlightVertexCapture1Color = palette.highlightVertexCapture1Color,
        highlightVertexCapture2Color = palette.highlightVertexCapture2Color,
        highlightVertexCapture3Color = palette.highlightVertexCapture3Color,
        highlightVertexAdjacent1Color = palette.highlightVertexAdjacent1Color,
        highlightVertexAdjacent2Color = palette.highlightVertexAdjacent2Color,
        highlightVertexAdjacent3Color = palette.highlightVertexAdjacent3Color,
        highlightVertexUpgrade1Color = palette.highlightVertexUpgrade1Color,
        highlightVertexUpgrade2Color = palette.highlightVertexUpgrade2Color,
        highlightVertexUpgrade3Color = palette.highlightVertexUpgrade3Color,
        highlightRegion1Color = palette.highlightRegion1Color,
        highlightRegion2Color = palette.highlightRegion2Color,
        highlightRegion3Color = palette.highlightRegion3Color,
    )
}