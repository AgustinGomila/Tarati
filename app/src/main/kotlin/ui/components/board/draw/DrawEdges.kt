package com.agustin.tarati.ui.components.board.draw

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.agustin.tarati.game.core.GameBoard.edges
import com.agustin.tarati.game.core.GameBoard.getVisualPosition
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.ui.components.board.BoardState
import com.agustin.tarati.ui.components.board.animation.EdgeHighlight
import com.agustin.tarati.ui.theme.BoardColors
import kotlin.math.atan2
import kotlin.math.cos
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
    colors: BoardColors
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

    // Efecto de pulso
    val pulseFactor = if (highlight.pulse) {
        val pulseTime = System.currentTimeMillis() % 1000L / 1000f
        (0.8f + 0.2f * sin(pulseTime * 2 * Math.PI).toFloat())
    } else {
        1f
    }

    // Calcular dirección (de toPos a fromPos)
    val direction = fromPos - toPos
    val angle = atan2(direction.y, direction.x)

    // Tamaños
    val circleRadius = 15f * pulseFactor
    val triangleWidth = 25f * pulseFactor

    // Crear estela triangular con base en toPos y punta en fromPos
    val path = Path().apply {
        // Base del triángulo (en toPos)
        val basePoint1 = Offset(
            x = toPos.x + triangleWidth * sin(angle),
            y = toPos.y - triangleWidth * cos(angle)
        )

        val basePoint2 = Offset(
            x = toPos.x - triangleWidth * sin(angle),
            y = toPos.y + triangleWidth * cos(angle)
        )

        // Punta del triángulo (en fromPos)
        val tipPoint = fromPos

        moveTo(basePoint1.x, basePoint1.y)
        lineTo(tipPoint.x, tipPoint.y)
        lineTo(basePoint2.x, basePoint2.y)
        close()
    }

    // Dibujar estela triangular
    drawPath(
        path = path,
        color = colors.highlightEdge1Color,
        alpha = 0.7f
    )

    // Círculo grande en el extremo "to" (base del triángulo)
    drawCircle(
        color = colors.highlightEdge2Color,
        center = toPos,
        radius = circleRadius,
        alpha = 0.9f
    )

    // Efecto de brillo
    drawCircle(
        color = colors.highlightEdge3Color.copy(alpha = 0.3f),
        center = toPos,
        radius = circleRadius * 1.8f,
        alpha = 0.4f
    )
}