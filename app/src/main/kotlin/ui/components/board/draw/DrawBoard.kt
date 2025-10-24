package com.agustin.tarati.ui.components.board.draw

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.agustin.tarati.game.core.GameBoard.BoardRegion
import com.agustin.tarati.game.core.GameBoard.domesticVertices
import com.agustin.tarati.game.core.GameBoard.getBoardRect
import com.agustin.tarati.game.core.GameBoard.getCentralRegions
import com.agustin.tarati.game.core.GameBoard.getCircumferenceRegions
import com.agustin.tarati.game.core.GameBoard.getDomesticRegions
import com.agustin.tarati.game.core.GameBoard.getVisualPosition
import com.agustin.tarati.game.core.GameBoard.vertices
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.ui.theme.BoardColors

fun DrawScope.drawBoardBackground(
    canvasSize: Size,
    orientation: BoardOrientation,
    colors: BoardColors,
    regionsVisible: Boolean,
    perimeterVisible: Boolean,
) {
    // Fondo base del tablero
    val boardRect = calculateBoardBoundingBox(
        getBoardRect(
            vertices = vertices,
            canvasSize = canvasSize,
            orientation = orientation
        ),
        canvasSize, 0.1f,
    )
    drawRoundRect(
        color = colors.boardBackground,
        topLeft = boardRect.topLeft,
        size = boardRect.size,
        cornerRadius = CornerRadius(16f)
    )

    if (perimeterVisible) {
        drawBoardPerimeter(canvasSize, orientation, colors)
    }

    if (regionsVisible) {
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

fun DrawScope.drawBoardPerimeter(
    canvasSize: Size,
    orientation: BoardOrientation,
    colors: BoardColors
) {
    val center = Offset(canvasSize.width / 2, canvasSize.height / 2)
    val radius = minOf(canvasSize.width, canvasSize.height) * 0.8f / 2

    val boardRect = calculateBoardBoundingBox(
        getBoardRect(
            vertices = domesticVertices,
            canvasSize = canvasSize,
            orientation = orientation
        ), canvasSize, 0.06f
    )

    drawRoundRect(
        color = colors.boardPerimeterColor,
        topLeft = boardRect.topLeft,
        size = boardRect.size,
        cornerRadius = CornerRadius(16f)
    )

    drawCircle(
        color = colors.boardPerimeterColor,
        style = Fill,
        center = center,
        radius = radius * 1.02f,
    )
}

private fun calculateBoardBoundingBox(
    rect: BoardRect,
    canvasSize: Size,
    margin: Float,
): BoardRect {
    // Añadir un margen para que el fondo se extienda un poco más allá de los vértices
    val margin = minOf(canvasSize.width, canvasSize.height) * margin

    return BoardRect(
        topLeft = Offset(rect.topLeft.x - margin, rect.topLeft.y - margin),
        size = Size(rect.size.width + 2 * margin, rect.size.height + 2 * margin),
    )
}

// Data class para representar un rectángulo del tablero
data class BoardRect(val topLeft: Offset, val size: Size)