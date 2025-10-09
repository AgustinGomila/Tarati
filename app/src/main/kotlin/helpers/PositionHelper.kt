package com.agustin.tarati.helpers

import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Position(val x: Float, val y: Float)

object PositionHelper {
    fun getPosition(vertexId: String, boardSize: Pair<Float, Float>, vWidth: Float): Position {
        val (width, height) = boardSize
        val centerX = width / 2
        val centerY = height / 2

        if (vertexId == "A1") {
            return Position(centerX, centerY)
        }

        val type = vertexId[0]
        val position = vertexId.substring(1).toInt()

        return when (type) {
            'B' -> {
                val angle = (position - 1) * (Math.PI / 3)
                Position(
                    x = centerX + vWidth * cos(angle + Math.PI / 2).toFloat(),
                    y = centerY + vWidth * sin(angle + Math.PI / 2).toFloat()
                )
            }

            'C' -> {
                val angle = (position - 1) * (Math.PI / 6) - Math.PI / 12 + Math.PI / 2
                val radius =
                    vWidth * (1 + sqrt(11.0 / 13)).toFloat() - (Math.PI / 12).toFloat() + (Math.PI / 2).toFloat()
                Position(
                    x = centerX + radius * cos(angle).toFloat(),
                    y = centerY + radius * sin(angle).toFloat()
                )
            }

            'D' -> {
                val down = if (position > 2) -1 else 1
                val left = if (position == 1 || position == 4) 1 else -1
                Position(
                    x = centerX + vWidth / 2 / left,
                    y = centerY + vWidth * 3 * down
                )
            }

            else -> Position(centerX, centerY)
        }
    }
}

object AdaptivePositionHelper {
    fun getPosition(
        vertexId: String,
        canvasSize: Pair<Float, Float>,
        vWidth: Float,
        isLandscape: Boolean = false
    ): Offset {
        val (width, height) = canvasSize

        // Si está en landscape, tratar el canvas como portrait para el cálculo
        val effectiveWidth = if (isLandscape) height else width
        val effectiveHeight = if (isLandscape) width else height

        // Luego usar la PositionHelper original con las dimensiones efectivas
        val basePos = PositionHelper.getPosition(vertexId, effectiveWidth to effectiveHeight, vWidth)
        val offset = Offset(basePos.x, basePos.y)

        // Si está en landscape, rotar la posición
        return if (isLandscape) {
            Offset(basePos.y, effectiveWidth - basePos.x)
        } else {
            offset
        }
    }
}