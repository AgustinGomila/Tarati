package com.agustin.tarati.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun getBoardColors(): BoardColors {
    return rememberBoardColors()
}

data class BoardColors(
    val blackCobBorderColor: Color,
    val blackCobColor: Color,
    val boardBackground: Color,
    val boardEdgeColor: Color,
    val boardGlowColor: Color,
    val boardPatternBorderColor: Color,
    val boardPatternColor1: Color,
    val boardPatternColor2: Color,
    val boardPatternColor3: Color,
    val boardVertexColor: Color,
    val neutralColor: Color,
    val selectionIndicatorColor: Color,
    val textColor: Color,
    val vertexAdjacentColor: Color,
    val vertexOccupiedColor: Color,
    val vertexSelectedColor: Color,
    val whiteCobBorderColor: Color,
    val whiteCobColor: Color,

    val highlightEdge1Color: Color,
    val highlightEdge2Color: Color,
    val highlightEdge3Color: Color,

    val highlightVertexCapture1Color: Color,
    val highlightVertexCapture2Color: Color,
    val highlightVertexCapture3Color: Color,
    val highlightVertexAdjacent1Color: Color,
    val highlightVertexAdjacent2Color: Color,
    val highlightVertexAdjacent3Color: Color,
    val highlightVertexUpgrade1Color: Color,
    val highlightVertexUpgrade2Color: Color,
    val highlightVertexUpgrade3Color: Color,

    val highlightRegion1Color: Color,
    val highlightRegion2Color: Color,
    val highlightRegion3Color: Color,
)