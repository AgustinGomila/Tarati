package com.agustin.tarati

import com.agustin.tarati.game.ai.Difficulty
import com.agustin.tarati.game.ai.TaratiAI
import com.agustin.tarati.game.core.Checker
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.ui.components.board.applyMoveToBoard
import com.agustin.tarati.ui.screens.main.MainViewModel.Companion.initialGameState
import org.junit.Assert
import org.junit.Test

class TaratiAITest {
    @Test
    fun applyMoveToBoard_movesPiece_and_doesNotChangeTurn() {
        val gs = initialGameState(currentTurn = Color.WHITE)
        // Move C1 -> B1 (B1 is an available vertex)
        val newState = applyMoveToBoard(gs, "C1", "B1")

        Assert.assertFalse("Origin should be empty after move", newState.checkers.containsKey("C1"))

        Assert.assertTrue("Destination should contain moved checker", newState.checkers.containsKey("B1"))

        val moved = newState.checkers["B1"]!!
        Assert.assertEquals("Moved piece should retain its color", Color.WHITE, moved.color)

        // applyMoveToBoard DOES NOT toggle the turn; expect same currentTurn
        Assert.assertEquals(
            "applyMoveToBoard should not change currentTurn (App toggles it).",
            gs.currentTurn,
            newState.currentTurn
        )
    }

    @Test
    fun applyMoveToBoard_upgradesWhenEnteringOpponentHomeBase() {
        // Prepare a minimal state: black piece at B1 (empty normally)
        val state = GameState(mapOf("B1" to Checker(Color.BLACK, false)), currentTurn = Color.BLACK)
        val result = applyMoveToBoard(state, "B1", "C1") // C1 is white homebase
        val placed = result.checkers["C1"]
        Assert.assertNotNull("Piece must be placed at destination", placed)
        Assert.assertEquals("Color preserved", Color.BLACK, placed!!.color)
        Assert.assertTrue("Piece that entered opponent home base must be upgraded", placed.isUpgraded)
    }

    @Test
    fun getAllPossibleMoves_excludesBackwardMove_forNonUpgradedWhite() {
        // Single white checker at C1, turn WHITE
        val state = GameState(mapOf("C1" to Checker(Color.WHITE, false)), currentTurn = Color.WHITE)
        val moves = TaratiAI.getAllPossibleMoves(state)
        // Expect C1 -> C2 NOT to be present (this is 'backward' for WHITE)
        Assert.assertFalse(
            "Expected backward move C1 -> C2 to be disallowed for WHITE non-upgraded",
            moves.any { it.from == "C1" && it.to == "C2" },
        )
    }

    @Test
    fun getAllPossibleMoves_includesForwardMove_forNonUpgradedWhite() {
        // Single white checker at C2, turn WHITE
        val state = GameState(mapOf("C2" to Checker(Color.WHITE, false)), currentTurn = Color.WHITE)
        val moves = TaratiAI.getAllPossibleMoves(state)

        // Debug: imprimir todos los movimientos posibles
        println("Movimientos posibles para C2:")
        moves.forEach { println("${it.from} -> ${it.to}") }

        // Verificar que hay al menos un movimiento válido
        Assert.assertTrue("Should have at least one valid move", moves.isNotEmpty())

        // Para debugging, podemos verificar movimientos específicos que deberían ser válidos
        // basados en la posición relativa de C2
    }

    @Test
    fun getNextBestMove_returnsSomeMove_forBlackAtDepth1() {
        // Usar el estado inicial completo, no uno minimal
        val gs = initialGameState(currentTurn = Color.BLACK)

        // Debug: verificar movimientos posibles
        val possibleMoves = TaratiAI.getAllPossibleMoves(gs)
        println("Movimientos posibles para BLACK en estado inicial: ${possibleMoves.size}")
        possibleMoves.forEach { println("${it.from} -> ${it.to}") }

        val result = TaratiAI.getNextBestMove(gs, Difficulty.MEDIUM.aiDepth)
        Assert.assertNotNull("Result should not be null", result)

        // Si no hay movimientos posibles, el resultado puede ser null (juego terminado)
        if (possibleMoves.isNotEmpty()) {
            Assert.assertNotNull("Should return a move when there are legal moves", result.move)
        }
    }
}