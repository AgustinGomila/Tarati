package com.agustin.tarati.ui.components.board.draw

import android.graphics.Paint
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.agustin.tarati.game.core.GameBoard.getVisualPosition
import com.agustin.tarati.game.core.GameBoard.vertices
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.ui.components.board.BoardState
import com.agustin.tarati.ui.components.board.animation.VertexHighlight
import com.agustin.tarati.ui.theme.BoardColors
import kotlin.math.sin

fun DrawScope.drawVertices(
    canvasSize: Size,
    vWidth: Float,
    selectedVertexId: String?,
    adjacentVertexes: List<String>,
    boardState: BoardState,
    colors: BoardColors
) {
    val gameState = boardState.gameState
    val orientation = boardState.boardOrientation
    val labelsVisible = boardState.labelsVisible
    val verticesVisible = boardState.verticesVisible

    if (verticesVisible) {
        vertices.forEach { vertexId ->
            val pos = getVisualPosition(vertexId, canvasSize.width, canvasSize.height, orientation)
            val cob = gameState.cobs[vertexId]

            val vertexColor = when {
                vertexId == selectedVertexId -> colors.vertexSelectedColor
                adjacentVertexes.contains(vertexId) -> colors.vertexHighlightColor
                cob != null -> colors.vertexOccupiedColor
                else -> colors.boardVertexColor
            }

            drawCircle(color = vertexColor, center = pos, radius = vWidth / 10)

            // Borde del vértice
            drawCircle(
                color = colors.textColor.copy(alpha = 0.3f),
                center = pos,
                radius = vWidth / 10,
                style = Stroke(width = 1f)
            )

            if (labelsVisible) {
                // Etiqueta del vértice
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        vertexId, pos.x - vWidth / 5, pos.y - vWidth / 5, Paint().apply {
                            color = colors.textColor.hashCode()
                            textSize = vWidth / 6
                            isAntiAlias = true
                        })
                }
            }
        }
    }
}

fun DrawScope.drawVertexHighlight(
    highlight: VertexHighlight,
    canvasSize: Size,
    orientation: BoardOrientation,
) {
    val pos = getVisualPosition(highlight.vertexId, canvasSize.width, canvasSize.height, orientation)
    val baseRadius = minOf(canvasSize.width, canvasSize.height) * 0.03f

    // Efecto de pulso si está activado
    val pulseFactor = if (highlight.pulse) {
        val pulseTime = System.currentTimeMillis() % 1000L / 1000f
        (0.7f + 0.3f * sin(pulseTime * 2 * Math.PI).toFloat())
    } else {
        1f
    }

    val pulseRadius = baseRadius * pulseFactor

    // Círculo exterior brillante
    drawCircle(
        color = highlight.color.copy(alpha = 0.3f),
        center = pos,
        radius = pulseRadius * 1.6f
    )

    // Círculo principal del highlight
    drawCircle(
        color = highlight.color,
        center = pos,
        radius = pulseRadius * 0.8f,
        style = Stroke(width = 3f)
    )

    // Núcleo brillante
    drawCircle(
        color = highlight.color,
        center = pos,
        radius = pulseRadius * 0.4f
    )

    // TODO: Si hay mensaje, dibujar texto (opcional)
    highlight.messageResId?.let {
        // drawContext.canvas.nativeCanvas.drawText(...)
    }
}