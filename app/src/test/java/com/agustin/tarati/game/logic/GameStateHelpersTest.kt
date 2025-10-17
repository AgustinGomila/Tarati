package  com.agustin.tarati.game.logic

import com.agustin.tarati.game.core.Cob
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.createGameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameStateHelpersTest {

    @Test
    fun modifyCob_addNewCob() {
        val initialState = GameState(emptyMap(), currentTurn = Color.WHITE)
        val newState = initialState.modifyCob("C1", Color.WHITE, false)

        val cob = newState.cobs["C1"]
        assertNotNull("Should add new cob", cob)
        assertEquals("Cob color should be WHITE", Color.WHITE, cob!!.color)
        assertFalse("Cob should not be upgraded", cob.isUpgraded)
    }

    @Test
    fun modifyCob_updateExistingCob() {
        val initialState = GameState(
            mapOf("C1" to Cob(Color.WHITE, false)),
            currentTurn = Color.WHITE
        )

        val newState = initialState.modifyCob("C1", Color.BLACK, true)

        val cob = newState.cobs["C1"]
        assertNotNull("Cob should exist", cob)
        assertEquals("Cob color should be updated", Color.BLACK, cob!!.color)
        assertTrue("Cob should be upgraded", cob.isUpgraded)
    }

    @Test
    fun modifyCob_partialUpdate() {
        val initialState = GameState(
            mapOf("C1" to Cob(Color.WHITE, false)),
            currentTurn = Color.WHITE
        )

        // Only update color
        val state1 = initialState.modifyCob("C1", Color.BLACK)
        val cob1 = state1.cobs["C1"]
        assertEquals("Color should be updated", Color.BLACK, cob1!!.color)
        assertFalse("Upgrade status should remain", cob1.isUpgraded)

        // Only update upgrade status
        val state2 = initialState.modifyCob("C1", isUpgraded = true)
        val cob2 = state2.cobs["C1"]
        assertEquals("Color should remain", Color.WHITE, cob2!!.color)
        assertTrue("Upgrade status should be updated", cob2.isUpgraded)
    }

    @Test
    fun modifyCob_removeCob() {
        val initialState = GameState(
            mapOf("C1" to Cob(Color.WHITE, false)),
            currentTurn = Color.WHITE
        )

        val newState = initialState.modifyCob("C1")

        assertFalse("Cob should be removed", newState.cobs.containsKey("C1"))
    }

    @Test
    fun moveCob_successfulMove() {
        val initialState = GameState(
            mapOf("C1" to Cob(Color.WHITE, false)),
            currentTurn = Color.WHITE
        )

        val newState = initialState.moveCob("C1", "B1")

        assertFalse("Original position should be empty", newState.cobs.containsKey("C1"))
        assertTrue("New position should have cob", newState.cobs.containsKey("B1"))

        val movedCob = newState.cobs["B1"]
        assertEquals("Cob should retain color", Color.WHITE, movedCob!!.color)
        assertFalse("Cob should retain upgrade status", movedCob.isUpgraded)
    }

    @Test
    fun moveCob_nonExistentCob() {
        val initialState = GameState(
            mapOf("C1" to Cob(Color.WHITE, false)),
            currentTurn = Color.WHITE
        )

        // Try to move non-existent cob
        val newState = initialState.moveCob("C2", "B1")

        // State should remain unchanged
        assertEquals("State should be unchanged", initialState, newState)
    }

    @Test
    fun withTurn_changesTurn() {
        val initialState = GameState(
            mapOf("C1" to Cob(Color.WHITE, false)),
            currentTurn = Color.WHITE
        )

        val newState = initialState.withTurn(Color.BLACK)

        assertEquals("Turn should be BLACK", Color.BLACK, newState.currentTurn)
        assertEquals("Cobs should remain the same", initialState.cobs, newState.cobs)
    }

    @Test
    fun createGameState_withBuilderPattern() {
        val state = createGameState {
            setTurn(Color.BLACK)
            setCob("C1", Color.WHITE, false)
            setCob("C7", Color.BLACK, true)
            moveCob("C1", "B1")
        }

        assertEquals("Turn should be BLACK", Color.BLACK, state.currentTurn)
        assertFalse("C1 should be empty", state.cobs.containsKey("C1"))
        assertTrue("B1 should have cob", state.cobs.containsKey("B1"))
        assertTrue("C7 should have upgraded cob", state.cobs.containsKey("C7"))

        val b1Cob = state.cobs["B1"]
        assertEquals("B1 cob should be WHITE", Color.WHITE, b1Cob!!.color)

        val c7Cob = state.cobs["C7"]
        assertTrue("C7 cob should be upgraded", c7Cob!!.isUpgraded)
    }

    @Test
    fun createGameState_emptyBuilder() {
        val state = createGameState {
            // No operations
        }

        assertNotNull("Should create valid state", state)
        assertNotNull("Should have cobs", state.cobs)
        assertNotNull("Should have current turn", state.currentTurn)
    }

    @Test
    fun helpers_areImmutable() {
        val initialState = GameState(
            mapOf("C1" to Cob(Color.WHITE, false)),
            currentTurn = Color.WHITE
        )

        // Apply multiple operations
        val state1 = initialState.modifyCob("C2", Color.BLACK, false)
        val state2 = state1.moveCob("C1", "B1")
        val state3 = state2.withTurn(Color.BLACK)

        // Original state should remain unchanged
        assertEquals(
            "Original state should be unchanged",
            mapOf("C1" to Cob(Color.WHITE, false)), initialState.cobs
        )
        assertEquals(
            "Original turn should be unchanged",
            Color.WHITE, initialState.currentTurn
        )

        // New states should have the changes
        assertTrue("State1 should have new cob", state1.cobs.containsKey("C2"))
        assertFalse("State2 should have moved cob", state2.cobs.containsKey("C1"))
        assertEquals("State3 should have new turn", Color.BLACK, state3.currentTurn)
    }
}