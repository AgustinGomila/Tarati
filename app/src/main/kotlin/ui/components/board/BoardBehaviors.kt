package com.agustin.tarati.ui.components.board

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.PointerInputScope
import com.agustin.tarati.game.core.GameBoard.findClosestVertex
import com.agustin.tarati.game.core.GameBoard.isValidMove
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.getValidVertex
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

    // Seleccionar pieza si no hay origen
    if (from == null) {
        selectPiece(gameState, to, tapEvents::onSelected)
        return
    }

    // Deseleccionar si toca la misma pieza
    if (to == from) {
        if (debug) println("Deselecting piece")
        tapEvents.onCancel()
        return
    }

    val fromColor = gameState.cobs[from]?.color ?: run {
        tapEvents.onCancel()
        return
    }

    if (debug) println("Attempting move from $from to $to")

    val toColor = gameState.cobs[to]?.color

    when {
        // Seleccionar otra pieza del mismo color
        toColor == fromColor -> selectPiece(gameState, to, tapEvents::onSelected)

        // Deseleccionar si toca pieza adversaria
        toColor != null -> {
            if (debug) println("Deselecting piece")
            tapEvents.onCancel()
        }

        // Intentar mover a casilla libre
        else -> movePiece(
            gameState = gameState,
            from = from,
            to = to,
            onMove = tapEvents::onMove,
            onInvalid = tapEvents::onInvalid,
            onCancel = tapEvents::onCancel
        )
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
    // Deseleccionar si toca la misma pieza
    if (to == from) {
        if (debug) println("Deselecting piece")
        onCancel()
        return
    }

    val isValid = isValidMove(gameState, from, to)
    if (debug) println("Move validation: $from -> $to = $isValid")

    if (isValid) {
        if (debug) println("Calling onMove with: $from, $to")
        onMove(from, to)
        return
    }

    if (debug) println("Move is invalid")

    // Si el movimiento es inválido, seleccionar la nueva pieza si es del jugador actual
    gameState.cobs[to]?.let { cob ->
        if (cob.color == gameState.currentTurn) {
            onInvalid(to, gameState.getValidVertex(from, cob))
        } else {
            onCancel()
        }
    } ?: onCancel()
}

fun selectPiece(
    gameState: GameState,
    from: String,
    onSelected: (from: String, valid: List<String>) -> Unit,
    debug: Boolean = false
) {
    val cob = gameState.cobs[from]
    if (debug) println("Checking piece: $cob at $from, currentTurn: ${gameState.currentTurn}")

    if (cob?.color != gameState.currentTurn) {
        if (debug) println("Cannot select: cob=$cob, currentTurn=${gameState.currentTurn}")
        return
    }

    if (debug) println("Piece selected: $from")

    val validMoves = gameState.getValidVertex(from, cob)
    onSelected(from, validMoves)

    if (debug) println("Highlighted moves: $validMoves")
}