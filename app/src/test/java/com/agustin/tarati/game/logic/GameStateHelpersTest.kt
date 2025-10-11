package  com.agustin.tarati.game.logic

import com.agustin.tarati.game.core.Checker
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.GameState
import org.junit.Assert
import org.junit.Test

class GameStateHelpersTest {

    @Test
    fun modifyChecker_addNewChecker() {
        val initialState = GameState(emptyMap(), currentTurn = Color.WHITE)
        val newState = initialState.modifyChecker("C1", Color.WHITE, false)

        val checker = newState.checkers["C1"]
        Assert.assertNotNull("Should add new checker", checker)
        Assert.assertEquals("Checker color should be WHITE", Color.WHITE, checker!!.color)
        Assert.assertFalse("Checker should not be upgraded", checker.isUpgraded)
    }

    @Test
    fun modifyChecker_updateExistingChecker() {
        val initialState = GameState(
            mapOf("C1" to Checker(Color.WHITE, false)),
            currentTurn = Color.WHITE
        )

        val newState = initialState.modifyChecker("C1", Color.BLACK, true)

        val checker = newState.checkers["C1"]
        Assert.assertNotNull("Checker should exist", checker)
        Assert.assertEquals("Checker color should be updated", Color.BLACK, checker!!.color)
        Assert.assertTrue("Checker should be upgraded", checker.isUpgraded)
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
        Assert.assertEquals("Color should be updated", Color.BLACK, checker1!!.color)
        Assert.assertFalse("Upgrade status should remain", checker1.isUpgraded)

        // Only update upgrade status
        val state2 = initialState.modifyChecker("C1", isUpgraded = true)
        val checker2 = state2.checkers["C1"]
        Assert.assertEquals("Color should remain", Color.WHITE, checker2!!.color)
        Assert.assertTrue("Upgrade status should be updated", checker2.isUpgraded)
    }

    @Test
    fun modifyChecker_removeChecker() {
        val initialState = GameState(
            mapOf("C1" to Checker(Color.WHITE, false)),
            currentTurn = Color.WHITE
        )

        val newState = initialState.modifyChecker("C1")

        Assert.assertFalse("Checker should be removed", newState.checkers.containsKey("C1"))
    }

    @Test
    fun moveChecker_successfulMove() {
        val initialState = GameState(
            mapOf("C1" to Checker(Color.WHITE, false)),
            currentTurn = Color.WHITE
        )

        val newState = initialState.moveChecker("C1", "B1")

        Assert.assertFalse("Original position should be empty", newState.checkers.containsKey("C1"))
        Assert.assertTrue("New position should have checker", newState.checkers.containsKey("B1"))

        val movedChecker = newState.checkers["B1"]
        Assert.assertEquals("Checker should retain color", Color.WHITE, movedChecker!!.color)
        Assert.assertFalse("Checker should retain upgrade status", movedChecker.isUpgraded)
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
        Assert.assertEquals("State should be unchanged", initialState, newState)
    }

    @Test
    fun withTurn_changesTurn() {
        val initialState = GameState(
            mapOf("C1" to Checker(Color.WHITE, false)),
            currentTurn = Color.WHITE
        )

        val newState = initialState.withTurn(Color.BLACK)

        Assert.assertEquals("Turn should be BLACK", Color.BLACK, newState.currentTurn)
        Assert.assertEquals("Checkers should remain the same", initialState.checkers, newState.checkers)
    }

    @Test
    fun createGameState_withBuilderPattern() {
        val state = createGameState {
            setTurn(Color.BLACK)
            setChecker("C1", Color.WHITE, false)
            setChecker("C7", Color.BLACK, true)
            moveChecker("C1", "B1")
        }

        Assert.assertEquals("Turn should be BLACK", Color.BLACK, state.currentTurn)
        Assert.assertFalse("C1 should be empty", state.checkers.containsKey("C1"))
        Assert.assertTrue("B1 should have checker", state.checkers.containsKey("B1"))
        Assert.assertTrue("C7 should have upgraded checker", state.checkers.containsKey("C7"))

        val b1Checker = state.checkers["B1"]
        Assert.assertEquals("B1 checker should be WHITE", Color.WHITE, b1Checker!!.color)

        val c7Checker = state.checkers["C7"]
        Assert.assertTrue("C7 checker should be upgraded", c7Checker!!.isUpgraded)
    }

    @Test
    fun createGameState_emptyBuilder() {
        val state = createGameState {
            // No operations
        }

        Assert.assertNotNull("Should create valid state", state)
        Assert.assertNotNull("Should have checkers", state.checkers)
        Assert.assertNotNull("Should have current turn", state.currentTurn)
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
        Assert.assertEquals(
            "Original state should be unchanged",
            mapOf("C1" to Checker(Color.WHITE, false)), initialState.checkers
        )
        Assert.assertEquals(
            "Original turn should be unchanged",
            Color.WHITE, initialState.currentTurn
        )

        // New states should have the changes
        Assert.assertTrue("State1 should have new checker", state1.checkers.containsKey("C2"))
        Assert.assertFalse("State2 should have moved checker", state2.checkers.containsKey("C1"))
        Assert.assertEquals("State3 should have new turn", Color.BLACK, state3.currentTurn)
    }
}