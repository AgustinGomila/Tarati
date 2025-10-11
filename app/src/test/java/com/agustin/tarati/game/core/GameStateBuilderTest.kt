package com.agustin.tarati.game.core

import com.agustin.tarati.game.logic.GameStateBuilder
import org.junit.Assert
import org.junit.Test

class GameStateBuilderTest {

    @Test
    fun builder_defaultState_returnsInitialState() {
        val builder = GameStateBuilder()
        val state = builder.build()

        Assert.assertNotNull("Builder should return a valid state", state)
        Assert.assertNotNull("State should have checkers", state.checkers)
        Assert.assertNotNull("State should have current turn", state.currentTurn)
    }

    @Test
    fun builder_setTurn_changesCurrentTurn() {
        val builder = GameStateBuilder()

        val stateWhite = builder.setTurn(Color.WHITE).build()
        Assert.assertEquals("Turn should be WHITE", Color.WHITE, stateWhite.currentTurn)

        val stateBlack = builder.setTurn(Color.BLACK).build()
        Assert.assertEquals("Turn should be BLACK", Color.BLACK, stateBlack.currentTurn)
    }

    @Test
    fun builder_setChecker_addsNewChecker() {
        val builder = GameStateBuilder()
        val state = builder
            .setChecker("C3", Color.WHITE, false)
            .build()

        val checker = state.checkers["C3"]
        Assert.assertNotNull("Checker should be added at C3", checker)
        Assert.assertEquals("Checker color should be WHITE", Color.WHITE, checker!!.color)
        Assert.assertFalse("Checker should not be upgraded", checker.isUpgraded)
    }

    @Test
    fun builder_setChecker_upgradesExistingChecker() {
        val initialBuilder = GameStateBuilder()
        val initialState = initialBuilder
            .setChecker("C3", Color.WHITE, false)
            .build()

        val builder = GameStateBuilder(initialState)
        val state = builder
            .setChecker("C3", Color.WHITE, true)
            .build()

        val checker = state.checkers["C3"]
        Assert.assertNotNull("Checker should exist at C3", checker)
        Assert.assertTrue("Checker should be upgraded", checker!!.isUpgraded)
    }

    @Test
    fun builder_setChecker_changesColor() {
        val initialBuilder = GameStateBuilder()
        val initialState = initialBuilder
            .setChecker("C3", Color.WHITE, false)
            .build()

        val builder = GameStateBuilder(initialState)
        val state = builder
            .setChecker("C3", Color.BLACK, false)
            .build()

        val checker = state.checkers["C3"]
        Assert.assertEquals("Checker color should be BLACK", Color.BLACK, checker!!.color)
    }

    @Test
    fun builder_removeChecker_removesExistingChecker() {
        val initialBuilder = GameStateBuilder()
        val initialState = initialBuilder
            .setChecker("C3", Color.WHITE, false)
            .build()

        Assert.assertTrue("Initial state should have checker at C3", initialState.checkers.containsKey("C3"))

        val builder = GameStateBuilder(initialState)
        val state = builder
            .removeChecker("C3")
            .build()

        Assert.assertFalse("Checker should be removed from C3", state.checkers.containsKey("C3"))
    }

    @Test
    fun builder_moveChecker_movesToNewPosition() {
        val initialBuilder = GameStateBuilder()
        val initialState = initialBuilder
            .setChecker("C1", Color.WHITE, false)
            .build()

        val builder = GameStateBuilder(initialState)
        val state = builder
            .moveChecker("C1", "B1")
            .build()

        Assert.assertFalse("Original position should be empty", state.checkers.containsKey("C1"))
        Assert.assertTrue("New position should contain checker", state.checkers.containsKey("B1"))

        val movedChecker = state.checkers["B1"]
        Assert.assertEquals("Moved checker should retain color", Color.WHITE, movedChecker!!.color)
        Assert.assertFalse("Moved checker should retain upgrade status", movedChecker.isUpgraded)
    }

    @Test
    fun builder_chainMultipleOperations() {
        val state = GameStateBuilder()
            .setTurn(Color.BLACK)
            .setChecker("C1", Color.WHITE, false)
            .setChecker("C7", Color.BLACK, true)
            .moveChecker("C1", "B1")
            .removeChecker("C7")
            .setChecker("C8", Color.BLACK, false)
            .build()

        Assert.assertEquals("Turn should be BLACK", Color.BLACK, state.currentTurn)
        Assert.assertFalse("C1 should be empty", state.checkers.containsKey("C1"))
        Assert.assertTrue("B1 should have checker", state.checkers.containsKey("B1"))
        Assert.assertFalse("C7 should be removed", state.checkers.containsKey("C7"))
        Assert.assertTrue("C8 should have checker", state.checkers.containsKey("C8"))

        val b1Checker = state.checkers["B1"]
        Assert.assertEquals("B1 checker should be WHITE", Color.WHITE, b1Checker!!.color)

        val c8Checker = state.checkers["C8"]
        Assert.assertEquals("C8 checker should be BLACK", Color.BLACK, c8Checker!!.color)
    }

    @Test
    fun builder_withCustomInitialState() {
        val customInitial = GameState(
            checkers = mapOf("A1" to Checker(Color.WHITE, true)),
            currentTurn = Color.BLACK
        )

        val builder = GameStateBuilder(customInitial)
        val state = builder
            .setChecker("B1", Color.BLACK, false)
            .build()

        Assert.assertTrue("Should retain custom initial checker", state.checkers.containsKey("A1"))
        Assert.assertTrue("Should add new checker", state.checkers.containsKey("B1"))
        Assert.assertEquals("Should retain custom turn", Color.BLACK, state.currentTurn)
    }
}