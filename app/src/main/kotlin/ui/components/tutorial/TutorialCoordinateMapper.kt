package com.agustin.tarati.ui.components.tutorial

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.agustin.tarati.game.core.GameBoard
import com.agustin.tarati.game.logic.BoardOrientation

class TutorialCoordinateMapper(
    private val boardWidth: Float,
    private val boardHeight: Float,
    private val orientation: BoardOrientation
) {
    fun getBubblePositionForVertex(
        vertexId: String,
        bubbleWidth: Dp = 320.dp,
        bubbleHeight: Dp = 280.dp
    ): BubbleConfig {
        val vertexPosition = GameBoard.getVisualPosition(
            vertexId = vertexId,
            canvasWidth = boardWidth,
            canvasHeight = boardHeight,
            orientation = orientation
        )

        // Posición simple: poner la burbuja cerca del vértice
        // Calculamos la posición basada en la ubicación del vértice en el tablero
        val bubblePosition = calculateSimpleBubblePosition(vertexX = vertexPosition.x, vertexY = vertexPosition.y)

        return BubbleConfig(
            position = bubblePosition,
            targetVertex = vertexId,
            width = bubbleWidth,
            height = bubbleHeight
        )
    }

    private fun calculateSimpleBubblePosition(
        vertexX: Float,
        vertexY: Float,
    ): BubblePosition {
        val screenCenterX = boardWidth / 2
        val screenCenterY = boardHeight / 2

        // Determinar en qué cuadrante está el vértice
        return when {
            // Vértice en la parte superior izquierda
            vertexX < screenCenterX && vertexY < screenCenterY -> BubblePosition.TOP_LEFT

            // Vértice en la parte superior derecha
            vertexX >= screenCenterX && vertexY < screenCenterY -> BubblePosition.TOP_RIGHT

            // Vértice en la parte inferior izquierda
            vertexX < screenCenterX && vertexY >= screenCenterY -> BubblePosition.BOTTOM_LEFT

            // Vértice en la parte inferior derecha
            else -> BubblePosition.BOTTOM_RIGHT
        }
    }
}