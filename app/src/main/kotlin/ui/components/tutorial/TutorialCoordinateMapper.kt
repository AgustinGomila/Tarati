package com.agustin.tarati.ui.components.tutorial

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.agustin.tarati.game.core.GameBoard
import com.agustin.tarati.game.logic.BoardOrientation
import kotlin.math.abs

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

        // Posición inteligente que usa CENTER cuando es apropiado
        val bubblePosition = calculateBubblePosition(
            vertexX = vertexPosition.x,
            vertexY = vertexPosition.y
        )

        return BubbleConfig(
            position = bubblePosition,
            targetVertex = vertexId,
            width = bubbleWidth,
            height = bubbleHeight
        )
    }

    private fun calculateBubblePosition(
        vertexX: Float,
        vertexY: Float,
    ): BubblePosition {
        val screenCenterX = boardWidth / 2
        val screenCenterY = boardHeight / 2

        // Definir umbrales para considerar "cerca del centro"
        val centerThresholdX = boardWidth * 0.3f
        val centerThresholdY = boardHeight * 0.3f

        val isNearCenterX = abs(vertexX - screenCenterX) < centerThresholdX
        val isNearCenterY = abs(vertexY - screenCenterY) < centerThresholdY

        // Si está cerca del centro en ambos ejes, usar posición central
        if (isNearCenterX && isNearCenterY) {
            return if (vertexY < screenCenterY) BubblePosition.BOTTOM_CENTER else BubblePosition.TOP_CENTER
        }

        // Si está cerca del centro horizontalmente pero no verticalmente
        if (isNearCenterX) {
            return if (vertexY < screenCenterY) BubblePosition.BOTTOM_CENTER else BubblePosition.TOP_CENTER
        }

        // Si está cerca del centro verticalmente pero no horizontalmente
        if (isNearCenterY) {
            return if (vertexX < screenCenterX) BubblePosition.CENTER_RIGHT else BubblePosition.CENTER_LEFT
        }

        // Para vértices lejos del centro, usar lógica de posición opuesta
        return when {
            // Vértice en parte superior izquierda -> Burbuja en inferior derecha
            vertexX < screenCenterX && vertexY < screenCenterY -> BubblePosition.BOTTOM_RIGHT

            // Vértice en parte superior derecha -> Burbuja en inferior izquierda
            vertexX >= screenCenterX && vertexY < screenCenterY -> BubblePosition.BOTTOM_LEFT

            // Vértice en parte inferior izquierda -> Burbuja en superior derecha
            vertexX < screenCenterX && vertexY >= screenCenterY -> BubblePosition.TOP_RIGHT

            // Vértice en parte inferior derecha -> Burbuja en superior izquierda
            else -> BubblePosition.TOP_LEFT
        }
    }
}