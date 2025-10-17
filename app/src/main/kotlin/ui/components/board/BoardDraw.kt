package com.agustin.tarati.ui.components.board

import android.graphics.Paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.agustin.tarati.game.core.Cob
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameBoard.edges
import com.agustin.tarati.game.core.GameBoard.getVisualPosition
import com.agustin.tarati.game.core.GameBoard.vertices
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.ui.theme.BoardColors

fun DrawScope.drawPiece(
    selectedVertexId: String?,
    vertexId: String,
    cob: Cob,
    colors: BoardColors,
    sizeFactor: Float = 1.2f,
) {
    // Dibujar borde + relleno
    val center = Offset(size.width / 2f, size.height / 2f)
    val baseRadius = minOf(size.width, size.height) / 2f * sizeFactor

    val (pieceColor, borderColor) = when (cob.color) {
        WHITE -> colors.whitePieceColor to colors.whitePieceBorderColor
        BLACK -> colors.blackPieceColor to colors.blackPieceBorderColor
    }

    drawCircle(color = borderColor, center = center, radius = baseRadius, style = Stroke(width = 3f))
    drawCircle(color = pieceColor, center = center, radius = baseRadius * 0.8f)

    if (cob.isUpgraded) {
        val upgradeColor = if (cob.color == WHITE)
            colors.blackPieceColor else colors.whitePieceColor

        drawCircle(
            color = upgradeColor,
            center = center,
            radius = baseRadius * 0.6f,
            style = Stroke(width = 2f)
        )
        drawCircle(
            color = upgradeColor,
            center = center,
            radius = baseRadius * 0.2f
        )
    }

    // Resaltado de selección
    if (vertexId == selectedVertexId) {
        drawCircle(
            color = colors.selectionIndicatorColor,
            center = center,
            radius = baseRadius * 1.2f,
            style = Stroke(width = 3f)
        )
    }
}

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

    vertices.forEach { vertexId ->
        val pos = getVisualPosition(vertexId, canvasSize.width, canvasSize.height, orientation)
        val cob = gameState.cobs[vertexId]

        val vertexColor = when {
            vertexId == selectedVertexId -> colors.vertexSelectedColor
            adjacentVertexes.contains(vertexId) -> colors.vertexHighlightColor
            cob != null -> colors.vertexOccupiedColor
            else -> colors.vertexDefaultColor
        }

        drawCircle(color = vertexColor, center = pos, radius = vWidth / 10)

        // Borde del vértice
        drawCircle(
            color = colors.textColor.copy(alpha = 0.3f), center = pos, radius = vWidth / 10, style = Stroke(width = 1f)
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

fun DrawScope.drawEdges(canvasSize: Size, orientation: BoardOrientation, colors: BoardColors) {
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
        drawLine(color = colors.edgeColor, start = fromPos, end = toPos, strokeWidth = 6f)
    }
}

fun DrawScope.drawAnimatedPiece(
    selectedVertexId: String?,
    vertexId: String,
    animatedPiece: AnimatedPiece,
    colors: BoardColors,
    sizeFactor: Float = 1.2f,
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val baseRadius = minOf(size.width, size.height) / 2f * sizeFactor

    // Determinar colores actuales considerando animaciones
    val currentCob = animatedPiece.cob
    val (pieceColor, borderColor) = when (currentCob.color) {
        WHITE -> colors.whitePieceColor to colors.whitePieceBorderColor
        BLACK -> colors.blackPieceColor to colors.blackPieceBorderColor
    }

    val invertedColor = when (currentCob.color) {
        WHITE -> colors.blackPieceColor
        BLACK -> colors.whitePieceColor
    }

    // Dibujar pieza base (siempre visible)
    drawCircle(color = borderColor, center = center, radius = baseRadius, style = Stroke(width = 3f))
    drawCircle(color = pieceColor, center = center, radius = baseRadius * 0.8f)

    // Animación de upgrade - círculo interno
    if (currentCob.isUpgraded) {
        val upgradeAlpha = animatedPiece.upgradeProgress
        if (upgradeAlpha > 0f) {
            val upgradeColor = invertedColor.copy(alpha = upgradeAlpha)

            // Círculo exterior de upgrade
            drawCircle(
                color = upgradeColor,
                center = center,
                radius = baseRadius * 0.6f,
                style = Stroke(width = 2f * upgradeAlpha)
            )

            // Punto central de upgrade
            drawCircle(
                color = upgradeColor,
                center = center,
                radius = baseRadius * 0.2f * upgradeAlpha
            )
        }
    }

    // Animación de conversión - efecto de inversión
    if (animatedPiece.isConverting) {
        val conversionAlpha = animatedPiece.conversionProgress

        // Efecto de aura durante la conversión
        if (conversionAlpha < 0.8f) {
            val auraAlpha = (1f - conversionAlpha) * 0.4f
            drawCircle(
                color = colors.selectionIndicatorColor.copy(alpha = auraAlpha),
                center = center,
                radius = baseRadius * 1.3f
            )
        }

        // Efecto de parpadeo
        if (conversionAlpha % 0.3f < 0.15f) {
            val flashAlpha = (1f - conversionAlpha) * 0.6f
            drawCircle(
                color = invertedColor.copy(alpha = flashAlpha),
                center = center,
                radius = baseRadius * 1.1f,
                style = Stroke(width = 2f)
            )
        }
    }

    // Resaltado de selección
    if (vertexId == selectedVertexId) {
        drawCircle(
            color = colors.selectionIndicatorColor,
            center = center,
            radius = baseRadius * 1.2f,
            style = Stroke(width = 3f)
        )
    }
}