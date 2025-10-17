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
    from: String?,
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

        closestVertex?.let { vertexId ->
            if (editorMode) {
                tapEvents.onEditPieceRequested(vertexId)
            } else {
                handleTap(
                    gameState = gameState,
                    from = from,
                    to = vertexId,
                    tapEvents = tapEvents,
                    debug = debug
                )
            }
        }
    }
}

fun handleTap(
    gameState: GameState,
    from: String?,
    to: String,
    tapEvents: TapEvents,
    debug: Boolean = false,
) {
    if (debug) println("TAP HANDLED: vertex=$to, selectedVertexId=$from")

    if (from == null) {
        // Seleccionar pieza
        selectPiece(
            gameState = gameState,
            from = to,
            onSelected = { from, valid -> tapEvents.onSelected(from, valid) })
    } else {
        // Mover pieza
        if (debug) println("Attempting move from $from to $to")

        if (to != from) {
            movePiece(
                gameState = gameState,
                from = from,
                to = to,
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
    from: String,
    to: String,
    onMove: (from: String, to: String) -> Unit,
    onInvalid: (from: String, valid: List<String>) -> Unit,
    onCancel: () -> Unit,
    debug: Boolean = false
) {
    if (to != from) {
        val isValid = isValidMove(gameState, from, to)
        if (debug) println("Move validation: $from -> $to = $isValid")

        if (isValid) {
            if (debug) println("Calling onMove with: $from, $to")
            onMove(from, to)
        } else {
            if (debug) println("Move is invalid")
            // Si el movimiento es inválido, seleccionar la nueva pieza si es del jugador actual
            val checker = gameState.checkers[to]
            if (checker != null && checker.color == gameState.currentTurn) {
                val validMoves = adjacencyMap[to]?.filter { to ->
                    !gameState.checkers.containsKey(to) && (checker.isUpgraded || isForwardMove(
                        checker.color,
                        to,
                        to
                    ))
                } ?: emptyList()
                onInvalid(to, validMoves)
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