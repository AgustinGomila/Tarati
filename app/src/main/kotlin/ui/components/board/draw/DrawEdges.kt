package com.agustin.tarati.ui.components.board.draw

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
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
    val edgesVisible = boardState.boardVisualState.edgesVisibles

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

    // Tamaños
    val circleRadius = 45f * pulseFactor
    val triangleWidth = 25f * pulseFactor
    val radius = triangleWidth // triangleWidth como radio del semicírculo

    // Dibujar estela triangular
    drawPath(
        path = createStellaPath(toPos, fromPos, direction, radius, triangleWidth),
        color = colors.highlightEdge1Color,
        alpha = 0.7f
    )

    // Dibujar brillo estela triangular
    drawPath(
        path = createStellaPath(toPos, fromPos, direction, radius * 1.3f, triangleWidth * 1.3f),
        color = colors.highlightEdge2Color,
        style = Stroke(18f, join = StrokeJoin.Round),
        alpha = 0.3f
    )

    // Efecto de brillo
    drawCircle(
        color = colors.highlightEdge3Color,
        center = toPos,
        radius = circleRadius * 1.3f,
        alpha = 0.2f
    )

    // Círculo grande en el extremo "to" (base del triángulo)
    drawCircle(
        color = colors.highlightEdge2Color,
        center = toPos,
        radius = circleRadius * 0.4f,
        alpha = 0.8f
    )
}

fun createStellaPath(toPos: Offset, fromPos: Offset, direction: Offset, radius: Float, triangleWidth: Float): Path {
    val angle = atan2(direction.y, direction.x)

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

    return Path().apply {
        // Rect que contiene el círculo centrado en toPos
        val circleRect = Rect(
            left = toPos.x - radius,
            top = toPos.y - radius,
            right = toPos.x + radius,
            bottom = toPos.y + radius
        )

        // Ángulos (en grados) de los puntos base relativos al centro (toPos)
        fun toDeg(rad: Double) = Math.toDegrees(rad).toFloat()
        val angleBase1 = toDeg(atan2((basePoint1.y - toPos.y).toDouble(), (basePoint1.x - toPos.x).toDouble()))
        val angleBase2 = toDeg(atan2((basePoint2.y - toPos.y).toDouble(), (basePoint2.x - toPos.x).toDouble()))

        // Calculamos dos opciones de sweep (cw positivo)
        val start = angleBase2
        val sweepCW = ((angleBase1 - start + 360f) % 360f)
        val sweepCCW = sweepCW - 360f

        // función para obtener punto medio del arco dado start+sweep
        fun midPointForSweep(startAngle: Float, sweep: Float): Offset {
            val midAngleRad = Math.toRadians((startAngle + sweep / 2.0).toDouble())
            return Offset(
                x = toPos.x + (radius * cos(midAngleRad)).toFloat(),
                y = toPos.y + (radius * sin(midAngleRad)).toFloat()
            )
        }

        // Vector "direction": fromPos - toPos
        // Elegimos el sweep que deje la mitad del arco apuntando *en sentido opuesto* a la punta
        val midCW = midPointForSweep(start, sweepCW)
        // val midCCW = midPointForSweep(start, sweepCCW)

        val dotCW = (midCW.x - toPos.x) * (direction.x) + (midCW.y - toPos.y) * (direction.y)
        // val dotCCW = (midCCW.x - toPos.x) * (direction.x) + (midCCW.y - toPos.y) * (direction.y)

        // Queremos que el punto medio del arco tenga proyección NEGATIVA sobre `direction`
        // (es decir: que apunte hacia el lado opuesto a la punta). Si dotCW < 0, escogemos sweepCW.
        val chosenSweep = if (dotCW < 0f) sweepCW else sweepCCW

        // Dibujamos la gota:
        moveTo(basePoint1.x, basePoint1.y)
        lineTo(tipPoint.x, tipPoint.y)
        lineTo(basePoint2.x, basePoint2.y)

        // Arc desde basePoint2 hasta basePoint1 (start = angleBase2)
        arcTo(
            rect = circleRect,
            startAngleDegrees = start,
            sweepAngleDegrees = chosenSweep,
            forceMoveTo = false
        )

        close()
    }
}