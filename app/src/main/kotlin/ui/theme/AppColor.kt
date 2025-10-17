package com.agustin.tarati.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppColors {
    @Composable
    fun getBoardColors(): BoardColors {
        val colorScheme = MaterialTheme.colorScheme
        return BoardColors(
            neutralColor = colorScheme.scrim,
            backgroundColor = colorScheme.surface,
            boardBackgroundColor = colorScheme.surfaceVariant,
            edgeColor = colorScheme.outline.copy(alpha = 0.6f),
            vertexDefaultColor = colorScheme.outline.copy(alpha = 0.8f),
            vertexOccupiedColor = colorScheme.onSurfaceVariant,
            vertexSelectedColor = colorScheme.primary,
            vertexHighlightColor = colorScheme.tertiary,
            textColor = colorScheme.onSurface,
            blackPieceColor = colorScheme.primary,
            whitePieceColor = colorScheme.onSecondary,
            blackPieceBorderColor = colorScheme.onPrimary,
            whitePieceBorderColor = colorScheme.secondary,
            selectionIndicatorColor = colorScheme.primary.copy(alpha = 0.3f)
        )
    }
}

data class BoardColors(
    val neutralColor: Color,
    val backgroundColor: Color,
    val boardBackgroundColor: Color,
    val edgeColor: Color,
    val vertexDefaultColor: Color,
    val vertexOccupiedColor: Color,
    val vertexSelectedColor: Color,
    val vertexHighlightColor: Color,
    val textColor: Color,
    val blackPieceColor: Color,
    val whitePieceColor: Color,
    val blackPieceBorderColor: Color,
    val whitePieceBorderColor: Color,
    val selectionIndicatorColor: Color
)