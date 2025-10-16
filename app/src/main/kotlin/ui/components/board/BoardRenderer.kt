package com.agustin.tarati.ui.components.board

import android.graphics.Paint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameBoard.edges
import com.agustin.tarati.game.core.GameBoard.findClosestVertex
import com.agustin.tarati.game.core.GameBoard.getVisualPosition
import com.agustin.tarati.game.core.GameBoard.vertices
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.ui.theme.AppColors.getBoardColors
import com.agustin.tarati.ui.theme.BoardColors
import kotlin.math.roundToInt

data class VisualPiece(
    val id: String,
    val color: Color,
    val isUpgraded: Boolean,
    val targetPos: Offset
)

/**
 * BoardRenderer: fondo + vertices/edges (Canvas) + piezas overlay animadas

 * - selectedPiece / validMoves: piezas y vértices resaltados
 * - boardState: contiene gameState: fuente de verdad
 *        y modificadores visuales y de comportamiento
 * - tapEvents: eventos sobre el tablero
 */
@Composable
fun BoardRenderer(
    modifier: Modifier = Modifier,
    selectedPiece: String?,
    validMoves: List<String>,
    boardState: BoardState,
    tapEvents: TapEvents,
    debug: Boolean = false
) {
    val colors = getBoardColors()
    val density = LocalDensity.current
    val visualWidth by lazy { (with(density) { 60.dp.toPx() }) }

    // Tamaño del área del tablero en px (onGloballyPositioned)
    var containerWidthPx by remember { mutableIntStateOf(0) }
    var containerHeightPx by remember { mutableIntStateOf(0) }

    val gameState = boardState.gameState
    val orientation = boardState.boardOrientation
    val editorMode = boardState.isEditing

    // Derivar lista de entidades visuales (posiciones absolutas en px)
    val visualPieces by remember(gameState, orientation, containerWidthPx, containerHeightPx) {
        derivedStateOf {
            if (containerWidthPx == 0 || containerHeightPx == 0) return@derivedStateOf emptyList()
            val w = containerWidthPx.toFloat()
            val h = containerHeightPx.toFloat()
            gameState.checkers.mapNotNull { (id, checker) ->
                val pos = getVisualPosition(id, w, h, orientation)
                VisualPiece(id = id, color = checker.color, isUpgraded = checker.isUpgraded, targetPos = pos)
            }
        }
    }

    // Cache simple para detectar entradas/salidas
    val prevIds = remember { mutableStateListOf<String>() }
    LaunchedEffect(visualPieces.map { it.id }) {
        // Lista de ids actual (para animar aparición/desaparición)
        prevIds.clear()
        prevIds.addAll(visualPieces.map { it.id })
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                containerWidthPx = coords.size.width
                containerHeightPx = coords.size.height
            }
            .pointerInput(gameState, selectedPiece, orientation, editorMode) {
                detectTapGestures { offset ->
                    val closestVertex = findClosestVertex(
                        tapOffset = offset,
                        canvasWidth = size.width.toFloat(),
                        canvasHeight = size.height.toFloat(),
                        maxTapDistance = visualWidth / 3,
                        orientation = orientation
                    )

                    closestVertex?.let { logicalVertexId ->
                        if (editorMode) {
                            tapEvents.onEditPieceRequested(logicalVertexId)
                        } else {
                            handleTap(
                                tappedVertex = logicalVertexId,
                                gameState = gameState,
                                selectedPiece = selectedPiece,
                                tapEvents = tapEvents,
                                debug = debug
                            )
                        }
                    }
                }
            }
    ) {
        // Dibujar fondo/edges/vertices con Canvas
        Canvas(modifier = Modifier.matchParentSize()) {

            // Fondo
            drawRect(color = colors.backgroundColor)
            val canvasSize = size

            // Círculo del tablero
            drawCircle(
                color = colors.boardBackgroundColor, radius = minOf(size.width, size.height) * 0.8f / 2,
                center = Offset(size.width / 2, size.height / 2)
            )

            drawEdges(
                drawScope = this,
                canvasSize = canvasSize,
                orientation = orientation,
                colors = colors
            )

            drawVertices(
                drawScope = this,
                canvasSize = canvasSize,
                vWidth = visualWidth,
                selectedPiece = selectedPiece,
                validMoves = validMoves,
                boardState = boardState,
                colors = colors
            )
        }

        // Overlay: piezas como composables independientes posicionadas
        visualPieces.forEach { piece ->
            key(piece.id) {
                // animar x,y objetivo
                val animX by animateFloatAsState(
                    targetValue = piece.targetPos.x,
                    animationSpec = tween(durationMillis = 260)
                )
                val animY by animateFloatAsState(
                    targetValue = piece.targetPos.y,
                    animationSpec = tween(durationMillis = 260)
                )

                // Tamaño visual de la pieza
                val piecePx = with(LocalDensity.current) { (60.dp.toPx() / 5f) }
                val pieceDp = with(LocalDensity.current) { piecePx.toDp() }

                // Posicionar centrando la pieza
                val offset = IntOffset(
                    (animX - piecePx).roundToInt(),
                    (animY - piecePx).roundToInt()
                )

                // Un Canvas pequeño para la pieza. Esto permite escalar/alfa.
                Box(
                    modifier = Modifier
                        .offset { offset }
                        .size(pieceDp * 2f) // tamaño razonable para el "slot" de la pieza
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawPiece(selectedPiece, piece, colors)
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawPiece(
    selectedPiece: String?,
    piece: VisualPiece,
    colors: BoardColors,
) {
    // Dibujar borde + relleno
    val center = Offset(size.width / 2f, size.height / 2f)
    val baseRadius = minOf(size.width, size.height) / 2f * 1.2f

    val (pieceColor, borderColor) = when (piece.color) {
        WHITE -> colors.whitePieceColor to colors.whitePieceBorderColor
        BLACK -> colors.blackPieceColor to colors.blackPieceBorderColor
    }

    drawCircle(color = borderColor, center = center, radius = baseRadius, style = Stroke(width = 3f))
    drawCircle(color = pieceColor, center = center, radius = baseRadius * 0.8f)

    if (piece.isUpgraded) {
        val upgradeColor = if (piece.color == WHITE)
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
    if (piece.id == selectedPiece) {
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