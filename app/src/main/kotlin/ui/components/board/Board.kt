package com.agustin.tarati.ui.components.board

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agustin.tarati.game.ai.TaratiAI
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameBoard
import com.agustin.tarati.game.core.GameBoard.vertices
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.game.logic.NormalizedCoord
import com.agustin.tarati.game.logic.PositionHelper
import com.agustin.tarati.game.logic.createGameState
import com.agustin.tarati.ui.preview.endGameState
import com.agustin.tarati.ui.preview.initialGameStateWithUpgrades
import com.agustin.tarati.ui.preview.midGameState
import com.agustin.tarati.ui.theme.AppColors.getBoardColors
import com.agustin.tarati.ui.theme.BoardColors
import com.agustin.tarati.ui.theme.TaratiTheme

@Composable
fun Board(
    modifier: Modifier = Modifier,
    gameState: GameState,
    onMove: (from: String, to: String) -> Unit,
    boardOrientation: BoardOrientation,
    selectedPiece: String? = null,
    highlightedMoves: List<String> = listOf()
) {
    val viewModel: BoardViewModel = viewModel()

    // Resetear la selección cuando el gameState cambia (nuevo turno)
    LaunchedEffect(gameState) {
        viewModel.resetSelection()
    }

    val vmSelectedPiece by viewModel.selectedPiece.collectAsState(selectedPiece)
    val vmHighlightedMoves by viewModel.highlightedMoves.collectAsState(highlightedMoves)

    // Calcular tamaño visual basado en el contexto
    val density = LocalDensity.current
    val vWidth = with(density) { 60.dp.toPx() } // Tamaño base adaptable
    val boardColors = getBoardColors()

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .pointerInput(gameState, vmSelectedPiece, boardOrientation) {
                detectTapGestures { offset ->
                    val closestVertex = GameBoard.findClosestVertex(
                        tapOffset = offset,
                        canvasWidth = size.width.toFloat(),
                        canvasHeight = size.height.toFloat(),
                        maxTapDistance = vWidth / 3,
                        orientation = boardOrientation
                    )

                    closestVertex?.let { logicalVertexId ->
                        handleTap(
                            logicalVertexId,
                            gameState,
                            vmSelectedPiece,
                            viewModel,
                            onMove
                        )
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawBoardContent(
                gameState = gameState,
                orientation = boardOrientation,
                vWidth = vWidth,
                vmSelectedPiece = vmSelectedPiece,
                vmHighlightedMoves = vmHighlightedMoves,
                canvasSize = size,
                colors = boardColors
            )
        }
    }
}

fun handleTap(
    tappedVertex: String,
    gameState: GameState,
    currentSelectedPiece: String?,
    viewModel: BoardViewModel,
    onMove: (String, String) -> Unit
) {
    println("TAP HANDLED: vertex=$tappedVertex, selectedPiece=$currentSelectedPiece")

    if (currentSelectedPiece == null) {
        // Seleccionar pieza
        val checker = gameState.checkers[tappedVertex]
        println("Checking piece: $checker at $tappedVertex, currentTurn: ${gameState.currentTurn}")

        if (checker != null && checker.color == gameState.currentTurn) {
            viewModel.updateSelectedPiece(tappedVertex)
            println("Piece selected: $tappedVertex")

            // Calcular movimientos válidos
            val validMoves = GameBoard.adjacencyMap[tappedVertex]?.filter { to ->
                val isValid = !gameState.checkers.containsKey(to) &&
                        (checker.isUpgraded || TaratiAI.isForwardMove(checker.color, tappedVertex, to))
                println("Move $tappedVertex -> $to: valid=$isValid")
                isValid
            } ?: emptyList()

            viewModel.updateHighlightedMoves(validMoves)
            println("Highlighted moves: $validMoves")
        } else {
            println("Cannot select: checker=$checker, currentTurn=${gameState.currentTurn}")
        }
    } else {
        // Mover pieza
        println("Attempting move from $currentSelectedPiece to $tappedVertex")

        if (tappedVertex != currentSelectedPiece) {
            val isValid = TaratiAI.isValidMove(gameState, currentSelectedPiece, tappedVertex)
            println("Move validation: $currentSelectedPiece -> $tappedVertex = $isValid")

            if (isValid) {
                println("Calling onMove with: $currentSelectedPiece, $tappedVertex")
                onMove(currentSelectedPiece, tappedVertex)
            } else {
                println("Move is invalid")
                // Si el movimiento es inválido, seleccionar la nueva pieza si es del jugador actual
                val checker = gameState.checkers[tappedVertex]
                if (checker != null && checker.color == gameState.currentTurn) {
                    viewModel.updateSelectedPiece(tappedVertex)
                    val validMoves = GameBoard.adjacencyMap[tappedVertex]?.filter { to ->
                        !gameState.checkers.containsKey(to) &&
                                (checker.isUpgraded || TaratiAI.isForwardMove(checker.color, tappedVertex, to))
                    } ?: emptyList()
                    viewModel.updateHighlightedMoves(validMoves)
                } else {
                    viewModel.resetSelection()
                }
            }
        } else {
            // Tocar la misma pieza: deseleccionar
            println("Deselecting piece")
            viewModel.resetSelection()
        }
    }
}

private fun DrawScope.drawBoardContent(
    gameState: GameState,
    orientation: BoardOrientation,
    vWidth: Float,
    vmSelectedPiece: String?,
    vmHighlightedMoves: List<String>,
    canvasSize: Size,
    colors: BoardColors
) {
    // Fondo
    drawRect(color = colors.backgroundColor)

    // Calcular área cuadrada para el tablero (igual que en getVisualPosition)
    val minDimension = minOf(canvasSize.width, canvasSize.height)
    val squareSize = minDimension * 0.8f
    val offsetX = (canvasSize.width - squareSize) / 2
    val offsetY = (canvasSize.height - squareSize) / 2
    val boardCenter = Offset(offsetX + squareSize / 2, offsetY + squareSize / 2)

    // Círculo del tablero - usar la misma área cuadrada
    drawCircle(
        color = colors.boardBackgroundColor,
        center = boardCenter,
        radius = squareSize / 2
    )

    // Aristas
    GameBoard.edges.forEach { (from, to) ->
        val fromPos = GameBoard.getVisualPosition(from, canvasSize.width, canvasSize.height, orientation)
        val toPos = GameBoard.getVisualPosition(to, canvasSize.width, canvasSize.height, orientation)
        drawLine(color = colors.edgeColor, start = fromPos, end = toPos, strokeWidth = 2f)
    }

    // Vértices
    vertices.forEach { vertexId ->
        val pos = GameBoard.getVisualPosition(vertexId, canvasSize.width, canvasSize.height, orientation)
        val checker = gameState.checkers[vertexId]

        val vertexColor = when {
            vertexId == vmSelectedPiece -> colors.vertexSelectedColor
            vmHighlightedMoves.contains(vertexId) -> colors.vertexHighlightColor
            checker != null -> colors.vertexOccupiedColor
            else -> colors.vertexDefaultColor
        }

        drawCircle(color = vertexColor, center = pos, radius = vWidth / 10)

        // Borde del vértice
        drawCircle(
            color = colors.textColor.copy(alpha = 0.3f),
            center = pos,
            radius = vWidth / 10,
            style = Stroke(width = 1f)
        )

        // Etiqueta del vértice (para debugging)
        drawContext.canvas.nativeCanvas.apply {
            drawText(
                vertexId,
                pos.x - vWidth / 6,
                pos.y - vWidth / 6,
                Paint().apply {
                    color = colors.textColor.hashCode()
                    textSize = vWidth / 8
                    isAntiAlias = true
                }
            )
        }
    }

    // Piezas
    gameState.checkers.forEach { (vertexId, checker) ->
        val pos = GameBoard.getVisualPosition(vertexId, canvasSize.width, canvasSize.height, orientation)
        val baseRadius = vWidth / 5

        val (pieceColor, borderColor) = when (checker.color) {
            WHITE -> colors.whitePieceColor to colors.whitePieceBorderColor
            BLACK -> colors.blackPieceColor to colors.blackPieceBorderColor
        }

        // Dibujar pieza base
        drawCircle(color = borderColor, center = pos, radius = baseRadius, style = Stroke(width = 3f))
        drawCircle(color = pieceColor, center = pos, radius = baseRadius * 0.8f)

        // Indicador de mejora
        if (checker.isUpgraded) {
            val upgradeColor = if (checker.color == WHITE) colors.blackPieceColor else colors.whitePieceColor
            drawCircle(color = upgradeColor, center = pos, radius = baseRadius * 0.6f, style = Stroke(width = 2f))
            drawCircle(color = upgradeColor, center = pos, radius = baseRadius * 0.3f)
        }

        // Resaltado de selección
        if (vertexId == vmSelectedPiece) {
            drawCircle(
                color = colors.selectionIndicatorColor,
                center = pos,
                radius = baseRadius * 1.2f,
                style = Stroke(width = 3f)
            )
        }
    }
}

fun applyMoveToBoard(prevState: GameState, from: String, to: String): GameState {
    val mutable = prevState.checkers.toMutableMap()
    val movedChecker = mutable[from] ?: return prevState
    // remove from
    mutable.remove(from)
    // place moved checker (copy to allow mutability of isUpgraded)
    var placedChecker = movedChecker

    // Check for upgrades when moved into opponent home base
    val whiteBase = GameBoard.homeBases[WHITE] ?: emptyList()
    val blackBase = GameBoard.homeBases[BLACK] ?: emptyList()
    if (whiteBase.contains(to) && movedChecker.color == BLACK) {
        placedChecker = movedChecker.copy(isUpgraded = true)
    } else if (blackBase.contains(to) && movedChecker.color == WHITE) {
        placedChecker = movedChecker.copy(isUpgraded = true)
    }
    mutable[to] = placedChecker

    // Flip adjacent checkers (for each edge containing 'to', flip the other vertex if opponent)
    for (edge in GameBoard.edges) {
        val (a, b) = edge
        if (a != to && b != to) continue

        val adjacent = if (a == to) b else a
        val adjChecker = mutable[adjacent]
        if (adjChecker == null || adjChecker.color == placedChecker.color) continue

        var newAdj = adjChecker.copy(color = placedChecker.color)
        // Check for upgrades for flipped piece
        if (whiteBase.contains(adjacent) && newAdj.color == BLACK) {
            newAdj = newAdj.copy(isUpgraded = true)
        } else if (blackBase.contains(adjacent) && newAdj.color == WHITE) {
            newAdj = newAdj.copy(isUpgraded = true)
        }
        mutable[adjacent] = newAdj
    }

    return GameState(mutable.toMap(), prevState.currentTurn)
}

val normalizedPositions: Map<String, NormalizedCoord> by lazy {
    val tempMap = mutableMapOf<String, NormalizedCoord>()
    val referenceSize = 1100f to 1100f // Tamaño de referencia para normalizar

    vertices.forEach { vertexId ->
        val position = PositionHelper.getPosition(vertexId, referenceSize, 250f)
        // Normalizar las coordenadas (0-1)
        val normalizedX = position.x / referenceSize.first
        val normalizedY = position.y / referenceSize.second
        tempMap[vertexId] = NormalizedCoord(normalizedX, normalizedY)
    }

    tempMap.toMap()
}

@Preview(showBackground = true, widthDp = 400, heightDp = 500)
@Composable
fun BoardPreview_PortraitWhite() {
    TaratiTheme {
        Board(
            gameState = initialGameStateWithUpgrades(),
            onMove = { from, to -> println("Move from $from to $to") },
            boardOrientation = BoardOrientation.PORTRAIT_WHITE
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun BoardPreview_LandscapeBlack() {
    TaratiTheme {
        Board(
            gameState = midGameState(),
            onMove = { from, to -> println("Move from $from to $to") },
            boardOrientation = BoardOrientation.LANDSCAPE_BLACK
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 500)
@Composable
fun BoardPreview_Custom() {
    TaratiTheme {
        val exampleGameState = createGameState {
            setTurn(WHITE)
            setChecker("C2", WHITE, true)
            setChecker("C8", BLACK, true)
            setChecker("B1", WHITE, false)
            setChecker("B4", BLACK, false)
            // Agregar piezas extra para testing
            setChecker("C5", WHITE, true)
            setChecker("C11", BLACK, true)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Board(
                gameState = exampleGameState,
                onMove = { from, to -> println("Move from $from to $to") },
                boardOrientation = BoardOrientation.PORTRAIT_WHITE
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 500)
@Composable
fun BoardPreview_BlackPlayer() {
    TaratiTheme(true) {
        val exampleGameState = endGameState(BLACK)
        Box(modifier = Modifier.fillMaxSize()) {
            Board(
                gameState = exampleGameState,
                onMove = { from, to -> println("Move from $from to $to") },
                boardOrientation = BoardOrientation.PORTRAIT_BLACK
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun BoardPreview_Landscape_BlackPlayer() {
    TaratiTheme {
        val exampleGameState = createGameState { setTurn(BLACK) }
        Box(modifier = Modifier.fillMaxSize()) {
            Board(
                gameState = exampleGameState,
                onMove = { from, to -> println("Move from $from to $to") },
                boardOrientation = BoardOrientation.LANDSCAPE_BLACK
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun BoardPreview_Landscape_Debug() {
    TaratiTheme(true) {
        val exampleGameState = createGameState {}
        Box(modifier = Modifier.fillMaxSize()) {
            Board(
                gameState = exampleGameState,
                onMove = { from, to -> println("Move from $from to $to") },
                boardOrientation = BoardOrientation.LANDSCAPE_WHITE
            )
        }
    }
}