package com.agustin.tarati.game.ai

import com.agustin.tarati.game.ai.TaratiAI.applyMoveToBoard
import com.agustin.tarati.game.ai.TaratiAI.getNextBestMove
import com.agustin.tarati.game.core.Cob
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.GameBoard.isValidMove
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.logic.isGameOver
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameIntegrationTest {

    @Test
    fun completeGameFlow() {
        // Start with initial state
        var state = GameState(
            mapOf(
                "C1" to Cob(Color.WHITE, false), "C7" to Cob(Color.BLACK, false)
            ), currentTurn = Color.WHITE
        )

        // White makes a move
        val whiteMove = getNextBestMove(state, Difficulty.MIN)
        assertNotNull("White should have a valid move", whiteMove.move)

        // Apply white move
        state = applyMoveToBoard(state, whiteMove.move!!.from, whiteMove.move.to)
        state = state.copy(currentTurn = Color.BLACK)

        // Black makes a move
        val blackMove = getNextBestMove(state, Difficulty.MIN)
        assertNotNull("Black should have a valid move", blackMove.move)

        // Apply black move
        state = applyMoveToBoard(state, blackMove.move!!.from, blackMove.move.to)
        state = state.copy(currentTurn = Color.WHITE)

        // Game should not be over yet
        assertFalse(
            "Game should not be over after first moves", state.isGameOver()
        )
    }

    @Test
    fun aiDepthPerformance() {
        val state = GameState(
            mapOf(
                "C1" to Cob(Color.WHITE, false),
                "C2" to Cob(Color.WHITE, false),
                "C7" to Cob(Color.BLACK, false),
                "C8" to Cob(Color.BLACK, false)
            ), currentTurn = Color.WHITE
        )

        // Test different depths
        val depths = listOf(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD)

        depths.forEach { depth ->
            val startTime = System.currentTimeMillis()
            val result = getNextBestMove(state, depth)
            val endTime = System.currentTimeMillis()

            assertNotNull("AI should return a move at depth $depth", result.move)
            assertTrue(
                "Move should be valid", isValidMove(state, result.move!!.from, result.move.to)
            )

            println("Depth $depth took ${endTime - startTime}ms")
        }
    }
}