package com.agustin.tarati.game.logic

import com.agustin.tarati.game.ai.TaratiAI.applyMoveToBoard
import com.agustin.tarati.game.core.Cob
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.GameBoard.normalizedPositions
import com.agustin.tarati.game.core.GameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardLogicTest {

    @Test
    fun applyMoveToBoard_movesCobToNewPosition() {
        val initialState = GameState(
            cobs = mapOf("C1" to Cob(Color.WHITE, false)),
            currentTurn = Color.WHITE
        )

        val newState = applyMoveToBoard(initialState, "C1", "B1")

        assertFalse(
            "Original position should be empty",
            newState.cobs.containsKey("C1")
        )
        assertTrue(
            "New position should contain cob",
            newState.cobs.containsKey("B1")
        )
        assertEquals(
            "Cob should retain color",
            Color.WHITE, newState.cobs["B1"]!!.color
        )
    }

    @Test
    fun applyMoveToBoard_upgradesWhiteInBlackHomeBase() {
        val initialState = GameState(
            cobs = mapOf("C6" to Cob(Color.WHITE, false)),
            currentTurn = Color.WHITE
        )

        // C7 is in black home base
        val newState = applyMoveToBoard(initialState, "C6", "C7")

        val cob = newState.cobs["C7"]
        assertNotNull("Cob should exist at C7", cob)
        assertTrue(
            "White cob in black home base should be upgraded",
            cob!!.isUpgraded
        )
    }

    @Test
    fun applyMoveToBoard_upgradesBlackInWhiteHomeBase() {
        val initialState = GameState(
            cobs = mapOf("C2" to Cob(Color.BLACK, false)),
            currentTurn = Color.BLACK
        )

        // C1 is in white home base
        val newState = applyMoveToBoard(initialState, "C2", "C1")

        val cob = newState.cobs["C1"]
        assertNotNull("Cob should exist at C1", cob)
        assertTrue(
            "Black cob in white home base should be upgraded",
            cob!!.isUpgraded
        )
    }

    @Test
    fun applyMoveToBoard_flipsAdjacentOpponentCobs() {
        val initialState = GameState(
            cobs = mapOf(
                "C1" to Cob(Color.WHITE, false),
                "C2" to Cob(Color.BLACK, false) // Adjacent to C1
            ),
            currentTurn = Color.WHITE
        )

        val newState = applyMoveToBoard(initialState, "C1", "B1")

        val flippedCob = newState.cobs["C2"]
        assertNotNull("Cob should still exist at C2", flippedCob)
        assertEquals(
            "Adjacent black cob should flip to white",
            Color.WHITE, flippedCob!!.color
        )
    }

    @Test
    fun applyMoveToBoard_doesNotFlipSameColorCobs() {
        val initialState = GameState(
            cobs = mapOf(
                "C1" to Cob(Color.WHITE, false),
                "C2" to Cob(Color.WHITE, false) // Same color, adjacent to C1
            ),
            currentTurn = Color.WHITE
        )

        val newState = applyMoveToBoard(initialState, "C1", "B1")

        val sameColorCob = newState.cobs["C2"]
        assertNotNull("Cob should still exist at C2", sameColorCob)
        assertEquals(
            "Same color cob should not flip",
            Color.WHITE, sameColorCob!!.color
        )
    }

    @Test
    fun applyMoveToBoard_returnsOriginalStateWhenFromNotFound() {
        val initialState = GameState(
            cobs = mapOf("C1" to Cob(Color.WHITE, false)),
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