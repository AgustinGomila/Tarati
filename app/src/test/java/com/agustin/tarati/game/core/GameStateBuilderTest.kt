package com.agustin.tarati.game.core

import com.agustin.tarati.game.logic.GameStateBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameStateBuilderTest {

    @Test
    fun builder_defaultState_returnsInitialState() {
        val builder = GameStateBuilder()
        val state = builder.build()

        assertNotNull("Builder should return a valid state", state)
        assertNotNull("State should have cobs", state.cobs)
        assertNotNull("State should have current turn", state.currentTurn)
    }

    @Test
    fun builder_setTurn_changesCurrentTurn() {
        val builder = GameStateBuilder()

        val stateWhite = builder.setTurn(CobColor.WHITE).build()
        assertEquals("Turn should be WHITE", CobColor.WHITE, stateWhite.currentTurn)

        val stateBlack = builder.setTurn(CobColor.BLACK).build()
        assertEquals("Turn should be BLACK", CobColor.BLACK, stateBlack.currentTurn)
    }

    @Test
    fun builder_setCob_addsNewCob() {
        val builder = GameStateBuilder()
        val state = builder
            .setCob("C3", CobColor.WHITE, false)
            .build()

        val cob = state.cobs["C3"]
        assertNotNull("Cob should be added at C3", cob)
        assertEquals("Cob color should be WHITE", CobColor.WHITE, cob!!.color)
        assertFalse("Cob should not be upgraded", cob.isUpgraded)
    }

    @Test
    fun builder_setCob_upgradesExistingCob() {
        val initialBuilder = GameStateBuilder()
        val initialState = initialBuilder
            .setCob("C3", CobColor.WHITE, false)
            .build()

        val builder = GameStateBuilder(initialState)
        val state = builder
            .setCob("C3", CobColor.WHITE, true)
            .build()

        val cob = state.cobs["C3"]
        assertNotNull("Cob should exist at C3", cob)
        assertTrue("Cob should be upgraded", cob!!.isUpgraded)
    }

    @Test
    fun builder_setCob_changesColor() {
        val initialBuilder = GameStateBuilder()
        val initialState = initialBuilder
            .setCob("C3", CobColor.WHITE, false)
            .build()

        val builder = GameStateBuilder(initialState)
        val state = builder
            .setCob("C3", CobColor.BLACK, false)
            .build()

        val cob = state.cobs["C3"]
        assertEquals("Cob color should be BLACK", CobColor.BLACK, cob!!.color)
    }

    @Test
    fun builder_removeCob_removesExistingCob() {
        val initialBuilder = GameStateBuilder()
        val initialState = initialBuilder
            .setCob("C3", CobColor.WHITE, false)
            .build()

        assertTrue("Initial state should have cob at C3", initialState.cobs.containsKey("C3"))

        val builder = GameStateBuilder(initialState)
        val state = builder
            .removeCob("C3")
            .build()

        assertFalse("Cob should be removed from C3", state.cobs.containsKey("C3"))
    }

    @Test
    fun builder_moveCob_movesToNewPosition() {
        val initialBuilder = GameStateBuilder()
        val initialState = initialBuilder
            .setCob("C1", CobColor.WHITE, false)
            .build()

        val builder = GameStateBuilder(initialState)
        val state = builder
            .moveCob("C1", "B1")
            .build()

        assertFalse("Original position should be empty", state.cobs.containsKey("C1"))
        assertTrue("New position should contain cob", state.cobs.containsKey("B1"))

        val movedCob = state.cobs["B1"]
        assertEquals("Moved cob should retain color", CobColor.WHITE, movedCob!!.color)
        assertFalse("Moved cob should retain upgrade status", movedCob.isUpgraded)
    }

    @Test
    fun builder_chainMultipleOperations() {
        val state = GameStateBuilder()
            .setTurn(CobColor.BLACK)
            .setCob("C1", CobColor.WHITE, false)
            .setCob("C7", CobColor.BLACK, true)
            .moveCob("C1", "B1")
            .removeCob("C7")
            .setCob("C8", CobColor.BLACK, false)
            .build()

        assertEquals("Turn should be BLACK", CobColor.BLACK, state.currentTurn)
        assertFalse("C1 should be empty", state.cobs.containsKey("C1"))
        assertTrue("B1 should have cob", state.cobs.containsKey("B1"))
        assertFalse("C7 should be removed", state.cobs.containsKey("C7"))
        assertTrue("C8 should have cob", state.cobs.containsKey("C8"))

        val b1Cob = state.cobs["B1"]
        assertEquals("B1 cob should be WHITE", CobColor.WHITE, b1Cob!!.color)

        val c8Cob = state.cobs["C8"]
        assertEquals("C8 cob should be BLACK", CobColor.BLACK, c8Cob!!.color)
    }

    @Test
    fun builder_withCustomInitialState() {
        val customInitial = GameState(
            cobs = mapOf("A1" to Cob(CobColor.WHITE, true)),
            currentTurn = CobColor.BLACK
        )

        val builder = GameStateBuilder(customInitial)
        val state = builder
            .setCob("B1", CobColor.BLACK, false)
            .build()

        assertTrue("Should retain custom initial cob", state.cobs.containsKey("A1"))
        assertTrue("Should add new cob", state.cobs.containsKey("B1"))
        assertEquals("Should retain custom turn", CobColor.BLACK, state.currentTurn)
    }
}