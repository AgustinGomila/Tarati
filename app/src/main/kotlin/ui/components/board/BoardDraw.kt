package com.agustin.tarati.ui.components.board

import android.graphics.Paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.agustin.tarati.game.core.Checker
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
    checker: Checker,
    colors: BoardColors,
    sizeFactor: Float = 1.2f,
) {
    // Dibujar borde + relleno
    val center = Offset(size.width / 2f, size.height / 2f)
    val baseRadius = minOf(size.width, size.height) / 2f * sizeFactor

    val (pieceColor, borderColor) = when (checker.color) {
        WHITE -> colors.whitePieceColor to colors.whitePieceBorderColor
        BLACK -> colors.blackPieceColor to colors.blackPieceBorderColor
    }

    drawCircle(color = borderColor, center = center, radius = baseRadius, style = Stroke(width = 3f))
    drawCircle(color = pieceColor, center = center, radius = baseRadius * 0.8f)

    if (checker.isUpgraded) {
        val upgradeColor = if (checker.color == WHITE)
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

fun drawVertices(
    drawScope: DrawScope,
    canvasSize: Size,
    vWidth: Float,
    selectedPiece: String?,
    validMoves: List<String>,
    boardState: BoardState,
    colors: BoardColors
) {
    val gameState = boardState.gameState
    val orientation = boardState.boardOrientation
    val labelsVisible = boardState.labelsVisible

    vertices.forEach { vertexId ->
        val pos = getVisualPosition(vertexId, canvasSize.width, canvasSize.height, orientation)
        val checker = gameState.checkers[vertexId]

        val vertexColor = when {
            vertexId == selectedPiece -> colors.vertexSelectedColor
            validMoves.contains(vertexId) -> colors.vertexHighlightColor
            checker != null -> colors.vertexOccupiedColor
            else -> colors.vertexDefaultColor
        }

        drawScope.drawCircle(color = vertexColor, center = pos, radius = vWidth / 10)

        // Borde del vértice
        drawScope.drawCircle(
            color = colors.textColor.copy(alpha = 0.3f), center = pos, radius = vWidth / 10, style = Stroke(width = 1f)
        )

        if (labelsVisible) {
            // Etiqueta del vértice
            drawScope.drawContext.canvas.nativeCanvas.apply {
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

fun drawEdges(drawScope: DrawScope, canvasSize: Size, orientation: BoardOrientation, colors: BoardColors) {
    edges.forEach { (from, to) ->
        val fromPos = getVisualPosition(
            logicalVertexId = from,
            canvasWidth = canvasSize.width,
            canvasHeight = canvasSize.height,
            orientation = orientation
        )
        val toPos = getVisualPosition(
            logicalVertexId = to,
            canvasWidth = canvasSize.width,
            canvasHeight = canvasSize.height,
            orientation = orientation
        )
        drawScope.drawLine(color = colors.edgeColor, start = fromPos, end = toPos, strokeWidth = 6f)
    }
}