package  com.agustin.tarati.game.logic

import com.agustin.tarati.game.core.Checker
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
    fun modifyChecker_addNewChecker() {
        val initialState = GameState(emptyMap(), currentTurn = Color.WHITE)
        val newState = initialState.modifyChecker("C1", Color.WHITE, false)

        val checker = newState.checkers["C1"]
        assertNotNull("Should add new checker", checker)
        assertEquals("Checker color should be WHITE", Color.WHITE, checker!!.color)
        assertFalse("Checker should not be upgraded", checker.isUpgraded)
    }

    @Test
    fun modifyChecker_updateExistingChecker() {
        val initialState = GameState(
            mapOf("C1" to Checker(Color.WHITE, false)),
            currentTurn = Color.WHITE
        )

        val newState = initialState.modifyChecker("C1", Color.BLACK, true)

        val checker = newState.checkers["C1"]
        assertNotNull("Checker should exist", checker)
        assertEquals("Checker color should be updated", Color.BLACK, checker!!.color)
        assertTrue("Checker should be upgraded", checker.isUpgraded)
    }

    @Test
    fun modifyChecker_partialUpdate() {
        val initialState = GameState(
            mapOf("C1" to Checker(Color.WHITE, false)),
            currentTurn = Color.WHITE
        )

        // Only update color
        val state1 = initialState.modifyChecker("C1", Color.BLACK)
        val checker1 = state1.checkers["C1"]
        assertEquals("Color should be updated", Color.BLACK, checker1!!.color)
        assertFalse("Upgrade status should remain", checker1.isUpgraded)

        // Only update upgrade status
        val state2 = initialState.modifyChecker("C1", isUpgraded = true)
        val checker2 = state2.checkers["C1"]
        assertEquals("Color should remain", Color.WHITE, checker2!!.color)
        assertTrue("Upgrade status should be updated", checker2.isUpgraded)
    }

    @Test
    fun modifyChecker_removeChecker() {
        val initialState = GameState(
            mapOf("C1" to Checker(Color.WHITE, false)),
            currentTurn = Color.WHITE
        )

        val newState = initialState.modifyChecker("C1")

        assertFalse("Checker should be removed", newState.checkers.containsKey("C1"))
    }

    @Test
    fun moveChecker_successfulMove() {
        val initialState = GameState(
            mapOf("C1" to Checker(Color.WHITE, false)),
            currentTurn = Color.WHITE
        )

        val newState = initialState.moveChecker("C1", "B1")

        assertFalse("Original position should be empty", newState.checkers.containsKey("C1"))
        assertTrue("New position should have checker", newState.checkers.containsKey("B1"))

        val movedChecker = newState.checkers["B1"]
        assertEquals("Checker should retain color", Color.WHITE, movedChecker!!.color)
        assertFalse("Checker should retain upgrade status", movedChecker.isUpgraded)
    }

    @Test
    fun moveChecker_nonExistentChecker() {
        val initialState = GameState(
            mapOf("C1" to Checker(Color.WHITE, false)),
            currentTurn = Color.WHITE
        )

        // Try to move non-existent checker
        val newState = initialState.moveChecker("C2", "B1")

        // State should remain unchanged
        assertEquals("State should be unchanged", initialState, newState)
    }

    @Test
    fun withTurn_changesTurn() {
        val initialState = GameState(
            mapOf("C1" to Checker(Color.WHITE, false)),
            currentTurn = Color.WHITE
        )

        val newState = initialState.withTurn(Color.BLACK)

        assertEquals("Turn should be BLACK", Color.BLACK, newState.currentTurn)
        assertEquals("Checkers should remain the same", initialState.checkers, newState.checkers)
    }

    @Test
    fun createGameState_withBuilderPattern() {
        val state = createGameState {
            setTurn(Color.BLACK)
            setChecker("C1", Color.WHITE, false)
            setChecker("C7", Color.BLACK, true)
            moveChecker("C1", "B1")
        }

        assertEquals("Turn should be BLACK", Color.BLACK, state.currentTurn)
        assertFalse("C1 should be empty", state.checkers.containsKey("C1"))
        assertTrue("B1 should have checker", state.checkers.containsKey("B1"))
        assertTrue("C7 should have upgraded checker", state.checkers.containsKey("C7"))

        val b1Checker = state.checkers["B1"]
        assertEquals("B1 checker should be WHITE", Color.WHITE, b1Checker!!.color)

        val c7Checker = state.checkers["C7"]
        assertTrue("C7 checker should be upgraded", c7Checker!!.isUpgraded)
    }

    @Test
    fun createGameState_emptyBuilder() {
        val state = createGameState {
            // No operations
        }

        assertNotNull("Should create valid state", state)
        assertNotNull("Should have checkers", state.checkers)
        assertNotNull("Should have current turn", state.currentTurn)
    }

    @Test
    fun helpers_areImmutable() {
        val initialState = GameState(
            mapOf("C1" to Checker(Color.WHITE, false)),
            currentTurn = Color.WHITE
        )

        // Apply multiple operations
        val state1 = initialState.modifyChecker("C2", Color.BLACK, false)
        val state2 = state1.moveChecker("C1", "B1")
        val state3 = state2.withTurn(Color.BLACK)

        // Original state should remain unchanged
        assertEquals(
            "Original state should be unchanged",
            mapOf("C1" to Checker(Color.WHITE, false)), initialState.checkers
        )
        assertEquals(
            "Original turn should be unchanged",
            Color.WHITE, initialState.currentTurn
        )

        // New states should have the changes
        assertTrue("State1 should have new checker", state1.checkers.containsKey("C2"))
        assertFalse("State2 should have moved checker", state2.checkers.containsKey("C1"))
        assertEquals("State3 should have new turn", Color.BLACK, state3.currentTurn)
    }
}