package com.agustin.tarati.ui.components.board.draw

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.agustin.tarati.game.core.GameBoard.BoardRegion
import com.agustin.tarati.game.core.GameBoard.getCentralRegions
import com.agustin.tarati.game.core.GameBoard.getCircumferenceRegions
import com.agustin.tarati.game.core.GameBoard.getDomesticRegions
import com.agustin.tarati.game.core.GameBoard.getVisualPosition
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.ui.theme.BoardColors
import kotlin.math.sin


fun DrawScope.drawBoardBackground(
    canvasSize: Size,
    orientation: BoardOrientation,
    colors: BoardColors,
    showPattern: Boolean = true,
    showGlow: Boolean = false
) {
    // Fondo base del tablero
    val boardRect = calculateBoardBoundingBox(canvasSize, orientation)
    drawRoundRect(
        color = colors.boardBackground,
        topLeft = boardRect.topLeft,
        size = boardRect.size,
        cornerRadius = CornerRadius(16f)
    )

    if (showPattern) {
        // Dibujar regiones centrales
        drawBoardPatternTwoColors(
            canvasSize = canvasSize,
            boardRegions = getCentralRegions(),
            surfaceColor1 = colors.boardPatternColor3,
            surfaceColor2 = colors.boardPatternColor2,
            borderColor = colors.boardPatternBorderColor,
            orientation = orientation
        )

        // Dibujar regiones de circunferencia
        drawBoardPatternTwoColors(
            canvasSize = canvasSize,
            boardRegions = getCircumferenceRegions(),
            surfaceColor1 = colors.boardPatternColor3,
            surfaceColor2 = colors.boardPatternColor1,
            borderColor = colors.boardPatternBorderColor,
            orientation = orientation,
        )

        // Dibujar regiones domésticas
        drawBoardPatternSingleColor(
            canvasSize = canvasSize,
            boardRegions = getDomesticRegions(),
            surfaceColor = colors.boardPatternColor1,
            borderColor = colors.boardPatternBorderColor,
            orientation = orientation
        )
    }

    if (showGlow) {
        drawBoardGlowEffect(canvasSize, colors)
    }
}

fun DrawScope.drawBoardPatternTwoColors(
    canvasSize: Size,
    boardRegions: List<BoardRegion>,
    surfaceColor1: Color,
    surfaceColor2: Color,
    borderColor: Color,
    orientation: BoardOrientation,
) {
    boardRegions.forEachIndexed { index, region ->
        val path = Path().apply {
            // Crear polígono para cada región
            region.vertices.forEachIndexed { vertexIndex, vertexId ->
                val pos = getVisualPosition(
                    vertexId,
                    canvasSize.width,
                    canvasSize.height,
                    orientation
                )
                if (vertexIndex == 0) {
                    moveTo(pos.x, pos.y)
                } else {
                    lineTo(pos.x, pos.y)
                }
            }
            close()
        }

        // Intercalar entre color1 y color2
        val regionColor = if (index % 2 == 0) {
            surfaceColor1.copy(alpha = 0.4f)
        } else {
            surfaceColor2.copy(alpha = 0.4f)
        }

        drawPath(
            path = path,
            color = regionColor,
            style = Fill
        )

        // Borde sutil entre casillas
        drawPath(
            path = path,
            color = borderColor.copy(alpha = 0.1f),
            style = Stroke(width = 1f)
        )
    }
}

fun DrawScope.drawBoardPatternSingleColor(
    canvasSize: Size,
    boardRegions: List<BoardRegion>,
    surfaceColor: Color,
    borderColor: Color,
    orientation: BoardOrientation
) {
    boardRegions.forEach { region ->
        val path = Path().apply {
            // Crear polígono para cada región
            region.vertices.forEachIndexed { vertexIndex, vertexId ->
                val pos = getVisualPosition(
                    vertexId,
                    canvasSize.width,
                    canvasSize.height,
                    orientation
                )
                if (vertexIndex == 0) {
                    moveTo(pos.x, pos.y)
                } else {
                    lineTo(pos.x, pos.y)
                }
            }
            close()
        }

        // Usar siempre el mismo color
        drawPath(
            path = path,
            color = surfaceColor.copy(alpha = 0.4f),
            style = Fill
        )

        // Borde sutil entre casillas
        drawPath(
            path = path,
            color = borderColor.copy(alpha = 0.1f),
            style = Stroke(width = 1f)
        )
    }
}

fun DrawScope.drawBoardGlowEffect(
    canvasSize: Size,
    colors: BoardColors
) {
    val center = Offset(canvasSize.width / 2, canvasSize.height / 2)
    val radius = minOf(canvasSize.width, canvasSize.height) * 0.8f / 2

    // Efecto de glow sutil en el borde
    val glowTime = System.currentTimeMillis() % 3000L / 3000f
    val pulse = (sin(glowTime * 2 * Math.PI).toFloat() * 0.1f) + 0.9f

    drawCircle(
        color = colors.boardGlowColor.copy(alpha = 0.2f * pulse),
        center = center,
        radius = radius * 1.02f,
        style = Stroke(width = 8f)
    )

    // Gradiente radial interior
    val radialGradient = Brush.radialGradient(
        colors = listOf(
            colors.boardGlowColor.copy(alpha = 0.05f),
            colors.boardGlowColor.copy(alpha = 0.0f)
        ),
        center = center,
        radius = radius * 0.8f
    )

    drawCircle(
        brush = radialGradient,
        center = center,
        radius = radius * 0.8f
    )
}

private fun calculateBoardBoundingBox(
    canvasSize: Size,
    orientation: BoardOrientation
): BoardRect {
    // Obtener todos los vértices de todas las regiones
    val allVertices = getCentralRegions().flatMap { it.vertices } +
            getCircumferenceRegions().flatMap { it.vertices } +
            getDomesticRegions().flatMap { it.vertices }

    // Calcular posiciones y encontrar los límites
    val positions = allVertices.distinct().map { vertexId ->
        getVisualPosition(vertexId, canvasSize.width, canvasSize.height, orientation)
    }

    val minX = positions.minOf { it.x }
    val maxX = positions.maxOf { it.x }
    val minY = positions.minOf { it.y }
    val maxY = positions.maxOf { it.y }

    // Añadir un margen para que el fondo se extienda un poco más allá de los vértices
    val margin = minOf(canvasSize.width, canvasSize.height) * 0.1f

    return BoardRect(
        topLeft = Offset(minX - margin, minY - margin),
        size = Size((maxX - minX) + 2 * margin, (maxY - minY) + 2 * margin)
    )
}

// Data class para representar un rectángulo del tablero
data class BoardRect(val topLeft: Offset, val size: Size)