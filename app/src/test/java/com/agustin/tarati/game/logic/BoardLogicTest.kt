package com.agustin.tarati.game.logic

import com.agustin.tarati.game.ai.TaratiAI.applyMoveToBoard
import com.agustin.tarati.game.ai.TaratiAI.normalizedPositions
import com.agustin.tarati.game.core.Checker
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.GameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardLogicTest {

    @Test
    fun applyMoveToBoard_movesCheckerToNewPosition() {
        val initialState = GameState(
            checkers = mapOf("C1" to Checker(Color.WHITE, false)),
            currentTurn = Color.WHITE
        )

        val newState = applyMoveToBoard(initialState, "C1", "B1")

        assertFalse(
            "Original position should be empty",
            newState.checkers.containsKey("C1")
        )
        assertTrue(
            "New position should contain checker",
            newState.checkers.containsKey("B1")
        )
        assertEquals(
            "Checker should retain color",
            Color.WHITE, newState.checkers["B1"]!!.color
        )
    }

    @Test
    fun applyMoveToBoard_upgradesWhiteInBlackHomeBase() {
        val initialState = GameState(
            checkers = mapOf("C6" to Checker(Color.WHITE, false)),
            currentTurn = Color.WHITE
        )

        // C7 is in black home base
        val newState = applyMoveToBoard(initialState, "C6", "C7")

        val checker = newState.checkers["C7"]
        assertNotNull("Checker should exist at C7", checker)
        assertTrue(
            "White checker in black home base should be upgraded",
            checker!!.isUpgraded
        )
    }

    @Test
    fun applyMoveToBoard_upgradesBlackInWhiteHomeBase() {
        val initialState = GameState(
            checkers = mapOf("C2" to Checker(Color.BLACK, false)),
            currentTurn = Color.BLACK
        )

        // C1 is in white home base
        val newState = applyMoveToBoard(initialState, "C2", "C1")

        val checker = newState.checkers["C1"]
        assertNotNull("Checker should exist at C1", checker)
        assertTrue(
            "Black checker in white home base should be upgraded",
            checker!!.isUpgraded
        )
    }

    @Test
    fun applyMoveToBoard_flipsAdjacentOpponentCheckers() {
        val initialState = GameState(
            checkers = mapOf(
                "C1" to Checker(Color.WHITE, false),
                "C2" to Checker(Color.BLACK, false) // Adjacent to C1
            ),
            currentTurn = Color.WHITE
        )

        val newState = applyMoveToBoard(initialState, "C1", "B1")

        val flippedChecker = newState.checkers["C2"]
        assertNotNull("Checker should still exist at C2", flippedChecker)
        assertEquals(
            "Adjacent black checker should flip to white",
            Color.WHITE, flippedChecker!!.color
        )
    }

    @Test
    fun applyMoveToBoard_doesNotFlipSameColorCheckers() {
        val initialState = GameState(
            checkers = mapOf(
                "C1" to Checker(Color.WHITE, false),
                "C2" to Checker(Color.WHITE, false) // Same color, adjacent to C1
            ),
            currentTurn = Color.WHITE
        )

        val newState = applyMoveToBoard(initialState, "C1", "B1")

        val sameColorChecker = newState.checkers["C2"]
        assertNotNull("Checker should still exist at C2", sameColorChecker)
        assertEquals(
            "Same color checker should not flip",
            Color.WHITE, sameColorChecker!!.color
        )
    }

    @Test
    fun applyMoveToBoard_returnsOriginalStateWhenFromNotFound() {
        val initialState = GameState(
            checkers = mapOf("C1" to Checker(Color.WHITE, false)),
            currentTurn = Color.WHITE
        )

        val newState = applyMoveToBoard(initialState, "C2", "B1") // C2 doesn't exist

        assertEquals(
            "Should return original state when from position not found",
            initialState, newState
        )
    }

    @Test
    fun normalizedPositions_containsAllVertices() {
        val vertices = listOf(
            "A1", "B1", "B2", "B3", "B4", "B5", "B6",
            "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "C10", "C11", "C12",
            "D1", "D2", "D3", "D4"
        )

        vertices.forEach { vertex ->
            assertTrue(
                "Normalized positions should contain $vertex",
                normalizedPositions.containsKey(vertex)
            )
        }
    }
}