package com.agustin.tarati.ui.components.board

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.PointerInputScope
import com.agustin.tarati.game.core.GameBoard.adjacencyMap
import com.agustin.tarati.game.core.GameBoard.findClosestVertex
import com.agustin.tarati.game.core.GameBoard.isForwardMove
import com.agustin.tarati.game.core.GameBoard.isValidMove
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.logic.BoardOrientation


suspend fun PointerInputScope.tapGestures(
    visualWidth: Float,
    gameState: GameState,
    selectedPiece: String?,
    orientation: BoardOrientation,
    editorMode: Boolean,
    tapEvents: TapEvents,
    debug: Boolean
) {
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

fun handleTap(
    tappedVertex: String,
    gameState: GameState,
    selectedPiece: String?,
    tapEvents: TapEvents,
    debug: Boolean = false,
) {
    if (debug) println("TAP HANDLED: vertex=$tappedVertex, selectedPiece=$selectedPiece")

    if (selectedPiece == null) {
        // Seleccionar pieza
        selectPiece(
            gameState = gameState,
            from = tappedVertex,
            onSelected = { from, valid -> tapEvents.onSelected(from, valid) })
    } else {
        // Mover pieza
        if (debug) println("Attempting move from $selectedPiece to $tappedVertex")

        if (tappedVertex != selectedPiece) {
            movePiece(
                gameState = gameState,
                selectedPiece = selectedPiece,
                tappedVertex = tappedVertex,
                onMove = { from, to -> tapEvents.onMove(from, to) },
                onInvalid = { from, valid -> tapEvents.onInvalid(from, valid) },
                onCancel = { tapEvents.onCancel() }
            )
        } else {
            // Tocar la misma pieza: deseleccionar
            if (debug) println("Deselecting piece")
            tapEvents.onCancel()
        }
    }
}

fun movePiece(
    gameState: GameState,
    selectedPiece: String,
    tappedVertex: String,
    onMove: (from: String, to: String) -> Unit,
    onInvalid: (from: String, valid: List<String>) -> Unit,
    onCancel: () -> Unit,
    debug: Boolean = false
) {
    if (tappedVertex != selectedPiece) {
        val isValid = isValidMove(gameState, selectedPiece, tappedVertex)
        if (debug) println("Move validation: $selectedPiece -> $tappedVertex = $isValid")

        if (isValid) {
            if (debug) println("Calling onMove with: $selectedPiece, $tappedVertex")
            onMove(selectedPiece, tappedVertex)
        } else {
            if (debug) println("Move is invalid")
            // Si el movimiento es inválido, seleccionar la nueva pieza si es del jugador actual
            val checker = gameState.checkers[tappedVertex]
            if (checker != null && checker.color == gameState.currentTurn) {
                val validMoves = adjacencyMap[tappedVertex]?.filter { to ->
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
        if (debug) println("Deselecting piece")
        onCancel()
    }
}

fun selectPiece(
    gameState: GameState,
    from: String,
    onSelected: (from: String, valid: List<String>) -> Unit,
    debug: Boolean = false
) {
    val checker = gameState.checkers[from]
    if (debug) println("Checking piece: $checker at $from, currentTurn: ${gameState.currentTurn}")

    if (checker != null && checker.color == gameState.currentTurn) {

        if (debug) println("Piece selected: $from")

        // Calcular movimientos válidos
        val validMoves = adjacencyMap[from]?.filter { to ->
            val isValid = !gameState.checkers.containsKey(to) && (checker.isUpgraded || isForwardMove(
                checker.color,
                from,
                to
            ))
            if (debug) println("Move $from -> $to: valid=$isValid")
            isValid
        } ?: emptyList()
        onSelected(from, validMoves)
        if (debug) println("Highlighted moves: $validMoves")
    } else {
        if (debug) println("Cannot select: checker=$checker, currentTurn=${gameState.currentTurn}")
    }
}