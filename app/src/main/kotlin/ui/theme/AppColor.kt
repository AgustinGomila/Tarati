package com.agustin.tarati.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun getBoardColors(): BoardColors {
    return rememberBoardColors()
}

data class BoardColors(
    val neutralColor: Color,
    val vertexOccupiedColor: Color,
    val vertexSelectedColor: Color,
    val vertexHighlightColor: Color,
    val textColor: Color,
    val blackCobColor: Color,
    val whiteCobColor: Color,
    val blackCobBorderColor: Color,
    val whiteCobBorderColor: Color,
    val selectionIndicatorColor: Color,
    val boardVertexColor: Color,
    val boardEdgeColor: Color,
    val boardBackground: Color,
    val boardPatternColor1: Color,
    val boardPatternColor2: Color,
    val boardPatternColor3: Color,
    val boardPatternBorderColor: Color,
    val boardGlowColor: Color,
)