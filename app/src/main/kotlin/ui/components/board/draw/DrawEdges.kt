package com.agustin.tarati.ui.components.board.draw

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.agustin.tarati.game.core.GameBoard.edges
import com.agustin.tarati.game.core.GameBoard.getVisualPosition
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.ui.components.board.BoardState
import com.agustin.tarati.ui.components.board.animation.EdgeHighlight
import com.agustin.tarati.ui.theme.BoardColors
import kotlin.math.sin

fun DrawScope.drawEdges(
    canvasSize: Size,
    orientation: BoardOrientation,
    boardState: BoardState,
    colors: BoardColors
) {
    val edgesVisible = boardState.edgesVisible

    if (edgesVisible) {
        edges.forEach { (from, to) ->
            val fromPos = getVisualPosition(
                vertexId = from,
                canvasWidth = canvasSize.width,
                canvasHeight = canvasSize.height,
                orientation = orientation
            )
            val toPos = getVisualPosition(
                vertexId = to,
                canvasWidth = canvasSize.width,
                canvasHeight = canvasSize.height,
                orientation = orientation
            )
            drawLine(color = colors.boardEdgeColor, start = fromPos, end = toPos, strokeWidth = 6f)
        }
    }
}

fun DrawScope.drawEdgeHighlight(
    highlight: EdgeHighlight,
    canvasSize: Size,
    orientation: BoardOrientation,
) {
    val fromPos = getVisualPosition(
        highlight.from,
        canvasSize.width,
        canvasSize.height,
        orientation
    )
    val toPos = getVisualPosition(
        highlight.to,
        canvasSize.width,
        canvasSize.height,
        orientation
    )

    // Efecto de pulso para edges
    val pulseFactor = if (highlight.pulse) {
        val pulseTime = System.currentTimeMillis() % 1000L / 1000f
        (0.8f + 0.2f * sin(pulseTime * 2 * Math.PI).toFloat())
    } else {
        1f
    }

    // Línea principal resaltada
    drawLine(
        color = highlight.color,
        start = fromPos,
        end = toPos,
        strokeWidth = 12f * pulseFactor,
        alpha = 0.8f
    )

    // Borde brillante
    drawLine(
        color = highlight.color.copy(alpha = 0.4f),
        start = fromPos,
        end = toPos,
        strokeWidth = 18f * pulseFactor,
        alpha = 0.3f
    )

    // Efecto de puntos en los extremos
    drawCircle(
        color = highlight.color,
        center = fromPos,
        radius = 8f * pulseFactor
    )
    drawCircle(
        color = highlight.color,
        center = toPos,
        radius = 8f * pulseFactor
    )
}