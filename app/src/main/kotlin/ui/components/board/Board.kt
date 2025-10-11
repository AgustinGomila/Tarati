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
import com.agustin.tarati.game.ai.TaratiAI.isForwardMove
import com.agustin.tarati.game.ai.TaratiAI.isValidMove
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameBoard
import com.agustin.tarati.game.core.GameBoard.getVisualPosition
import com.agustin.tarati.game.core.GameBoard.vertices
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.logic.BoardOrientation
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
    newGame: Boolean = false,
    onResetCompleted: () -> Unit = {},
    onMove: (from: String, to: String) -> Unit,
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE,
    viewModel: BoardViewModel = viewModel()
) {
    LaunchedEffect(newGame) {
        if (newGame) {
            viewModel.resetSelection()
            onResetCompleted()
        }
    }

    val vmSelectedPiece by viewModel.selectedPiece.collectAsState()
    val vmValidMoves by viewModel.validMoves.collectAsState()

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
                            logicalVertexId, gameState, vmSelectedPiece, onMove, viewModel
                        )
                    }
                }
            }) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawBoardContent(
                gameState = gameState,
                orientation = boardOrientation,
                vWidth = vWidth,
                selectedPiece = vmSelectedPiece,
                highlightedMoves = vmValidMoves,
                canvasSize = size,
                colors = boardColors
            )
        }
    }
}

fun handleTap(
    tappedVertex: String,
    gameState: GameState,
    selectedPiece: String?,
    onMove: (String, String) -> Unit,
    viewModel: BoardViewModel
) {
    println("TAP HANDLED: vertex=$tappedVertex, selectedPiece=$selectedPiece")

    if (selectedPiece == null) {
        // Seleccionar pieza
        selectPiece(
            gameState = gameState,
            tappedVertex = tappedVertex,
            onSelected = { piece, moves ->
                viewModel.updateSelectedPiece(piece)
                viewModel.updateValidMoves(moves)
            })
    } else {
        // Mover pieza
        println("Attempting move from $selectedPiece to $tappedVertex")

        if (tappedVertex != selectedPiece) {
            movePiece(
                gameState = gameState,
                selectedPiece = selectedPiece,
                tappedVertex = tappedVertex,
                onMove = { from, to ->
                    onMove(from, to)
                    viewModel.resetSelection()
                },
                onInvalid = { from, valid ->
                    viewModel.updateSelectedPiece(from)
                    viewModel.updateValidMoves(valid)
                },
                onCancel = { viewModel.resetSelection() }
            )
        } else {
            // Tocar la misma pieza: deseleccionar
            println("Deselecting piece")
            viewModel.resetSelection()
        }
    }
}

private fun movePiece(
    gameState: GameState,
    selectedPiece: String,
    tappedVertex: String,
    onMove: (from: String, to: String) -> Unit,
    onInvalid: (from: String, valid: List<String>) -> Unit,
    onCancel: () -> Unit
) {
    if (tappedVertex != selectedPiece) {
        val isValid = isValidMove(gameState, selectedPiece, tappedVertex)
        println("Move validation: $selectedPiece -> $tappedVertex = $isValid")

        if (isValid) {
            println("Calling onMove with: $selectedPiece, $tappedVertex")
            onMove(selectedPiece, tappedVertex)
        } else {
            println("Move is invalid")
            // Si el movimiento es inválido, seleccionar la nueva pieza si es del jugador actual
            val checker = gameState.checkers[tappedVertex]
            if (checker != null && checker.color == gameState.currentTurn) {
                val validMoves = GameBoard.adjacencyMap[tappedVertex]?.filter { to ->
                    !gameState.checkers.containsKey(to) && (checker.isUpgraded || isForwardMove(
                        checker.color,
                        tappedVertex,
                        to
                    ))
                } ?: emptyList()
                onInvalid(tappedVertex, validMoves)
            } else {
                onCancel()
            }
        }
    } else {
        // Tocar la misma pieza: deseleccionar
        println("Deselecting piece")
        onCancel()
    }
}

private fun selectPiece(gameState: GameState, tappedVertex: String, onSelected: (String, List<String>) -> Unit) {
    val checker = gameState.checkers[tappedVertex]
    println("Checking piece: $checker at $tappedVertex, currentTurn: ${gameState.currentTurn}")

    if (checker != null && checker.color == gameState.currentTurn) {

        println("Piece selected: $tappedVertex")

        // Calcular movimientos válidos
        val validMoves = GameBoard.adjacencyMap[tappedVertex]?.filter { to ->
            val isValid = !gameState.checkers.containsKey(to) && (checker.isUpgraded || isForwardMove(
                checker.color,
                tappedVertex,
                to
            ))
            println("Move $tappedVertex -> $to: valid=$isValid")
            isValid
        } ?: emptyList()
        onSelected(tappedVertex, validMoves)
        println("Highlighted moves: $validMoves")
    } else {
        println("Cannot select: checker=$checker, currentTurn=${gameState.currentTurn}")
    }
}

private fun DrawScope.drawBoardContent(
    gameState: GameState,
    orientation: BoardOrientation,
    vWidth: Float,
    selectedPiece: String?,
    highlightedMoves: List<String>,
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
        color = colors.boardBackgroundColor, center = boardCenter, radius = squareSize / 2
    )

    // Aristas
    GameBoard.edges.forEach { (from, to) ->
        val fromPos = getVisualPosition(from, canvasSize.width, canvasSize.height, orientation)
        val toPos = getVisualPosition(to, canvasSize.width, canvasSize.height, orientation)
        drawLine(color = colors.edgeColor, start = fromPos, end = toPos, strokeWidth = 6f)
    }

    // Vértices
    vertices.forEach { vertexId ->
        val pos = getVisualPosition(vertexId, canvasSize.width, canvasSize.height, orientation)
        val checker = gameState.checkers[vertexId]

        val vertexColor = when {
            vertexId == selectedPiece -> colors.vertexSelectedColor
            highlightedMoves.contains(vertexId) -> colors.vertexHighlightColor
            checker != null -> colors.vertexOccupiedColor
            else -> colors.vertexDefaultColor
        }

        drawCircle(color = vertexColor, center = pos, radius = vWidth / 10)

        // Borde del vértice
        drawCircle(
            color = colors.textColor.copy(alpha = 0.3f), center = pos, radius = vWidth / 10, style = Stroke(width = 1f)
        )

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

    // Piezas
    gameState.checkers.forEach { (vertexId, checker) ->
        val pos = getVisualPosition(vertexId, canvasSize.width, canvasSize.height, orientation)
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
            val upgradeColor =
                if (checker.color == WHITE) colors.blackPieceColor
                else colors.whitePieceColor

            drawCircle(
                color = upgradeColor,
                center = pos,
                radius = baseRadius * 0.6f,
                style = Stroke(width = 2f)
            )
            drawCircle(
                color = upgradeColor,
                center = pos,
                radius = baseRadius * 0.2f
            )
        }

        // Resaltado de selección
        if (vertexId == selectedPiece) {
            drawCircle(
                color = colors.selectionIndicatorColor,
                center = pos,
                radius = baseRadius * 1.2f,
                style = Stroke(width = 3f)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 500)
@Composable
fun BoardPreview_PortraitWhite() {
    TaratiTheme {
        Board(
            gameState = initialGameStateWithUpgrades(),
            onMove = { from, to -> println("Move from $from to $to") },
            boardOrientation = BoardOrientation.PORTRAIT_WHITE,
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
            boardOrientation = BoardOrientation.LANDSCAPE_BLACK,
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

        val vm = viewModel<BoardViewModel>().apply {
            updateSelectedPiece("B1")
            updateValidMoves(listOf("B2", "A1", "B6"))
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Board(
                gameState = exampleGameState,
                onMove = { from, to -> println("Move from $from to $to") },
                boardOrientation = BoardOrientation.PORTRAIT_WHITE,
                viewModel = vm,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 500)
@Composable
fun BoardPreview_BlackPlayer() {
    TaratiTheme(true) {
        val exampleGameState = endGameState(BLACK)
        val vm = viewModel<BoardViewModel>().apply {
            updateSelectedPiece("A1")
            updateValidMoves(listOf("B1", "B2", "B3", "B4", "B5", "B6"))
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Board(
                gameState = exampleGameState,
                onMove = { from, to -> println("Move from $from to $to") },
                boardOrientation = BoardOrientation.PORTRAIT_BLACK,
                viewModel = vm,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun BoardPreview_Landscape_BlackPlayer() {
    TaratiTheme {
        val exampleGameState = createGameState { setTurn(BLACK) }
        val vm = viewModel<BoardViewModel>().apply {
            updateSelectedPiece("C2")
            updateValidMoves(listOf("C9", "B4", "B5"))
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Board(
                gameState = exampleGameState,
                onMove = { from, to -> println("Move from $from to $to") },
                boardOrientation = BoardOrientation.LANDSCAPE_BLACK,
                viewModel = vm,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun BoardPreview_Landscape_Debug() {
    TaratiTheme(true) {
        val exampleGameState = createGameState {}
        val vm = viewModel<BoardViewModel>().apply {
            updateSelectedPiece("C2")
            updateValidMoves(listOf("C3", "B2", "B1"))
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Board(
                gameState = exampleGameState,
                onMove = { from, to -> println("Move from $from to $to") },
                boardOrientation = BoardOrientation.LANDSCAPE_WHITE,
                viewModel = vm,
            )
        }
    }
}