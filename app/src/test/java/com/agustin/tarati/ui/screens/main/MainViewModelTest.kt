package com.agustin.tarati.ui.screens.main

import com.agustin.tarati.game.core.Cob
import com.agustin.tarati.game.core.CobColor
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.core.cleanGameState
import com.agustin.tarati.game.core.initialGameState
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.koin.core.context.GlobalContext.stopKoin

class MainViewModelTest {

    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        viewModel = MainViewModel()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun initialGameState_hasCorrectSetup() {
        val gameState = initialGameState()

        assertEquals("Initial turn should be WHITE", CobColor.WHITE, gameState.currentTurn)
        assertEquals("Should have 8 cobs total", 8, gameState.cobs.size)
        assertEquals(
            "Should have 4 white cobs",
            4,
            gameState.cobs.values.count { it.color == CobColor.WHITE })
        assertEquals(
            "Should have 4 black cobs",
            4,
            gameState.cobs.values.count { it.color == CobColor.BLACK })

        // Verify specific positions
        assertTrue("C1 should have white cob", gameState.cobs["C1"]?.color == CobColor.WHITE)
        assertTrue("C7 should have black cob", gameState.cobs["C7"]?.color == CobColor.BLACK)
        assertTrue("D1 should have white cob", gameState.cobs["D1"]?.color == CobColor.WHITE)
        assertTrue("D3 should have black cob", gameState.cobs["D3"]?.color == CobColor.BLACK)
    }

    @Test
    fun cleanGameState_hasNoCobs() {
        val gameState = cleanGameState()

        assertTrue("Clean state should have no cobs", gameState.cobs.isEmpty())
        assertEquals("Turn should be WHITE by default", CobColor.WHITE, gameState.currentTurn)
    }

    @Test
    fun cleanGameState_withCustomTurn() {
        val gameState = cleanGameState(CobColor.BLACK)

        assertTrue("Clean state should have no cobs", gameState.cobs.isEmpty())
        assertEquals("Turn should be BLACK", CobColor.BLACK, gameState.currentTurn)
    }

    @Test
    fun updateGameState_changesState() {
        val newState = GameState(
            cobs = mapOf("A1" to Cob(CobColor.WHITE, true)),
            currentTurn = CobColor.BLACK
        )

        viewModel.gameManager.updateGameState(newState)

        assertEquals("Game state should be updated", newState, viewModel.gameManager.gameState.value)
    }

    @Test
    fun updateHistory_changesHistory() {
        val move = Move("C1", "B1")
        val gameState = initialGameState()
        val history = listOf(Pair(move, gameState))

        viewModel.gameManager.addMove(move, gameState)

        assertEquals("History should be updated", history, viewModel.gameManager.history.value)
    }

    @Test
    fun updateAIEnabled_changesAIState() {
        viewModel.updateAIEnabled(false)

        assertFalse("AI should be disabled", viewModel.aIEnabled.value)
    }

    @Test
    fun updatePlayerSide_changesPlayerSide() {
        viewModel.updatePlayerSide(CobColor.BLACK)

        assertEquals("Player side should be BLACK", CobColor.BLACK, viewModel.playerSide.value)
    }

    @Test
    fun initialState_hasDefaultValues() {
        assertEquals(
            "Initial game state should match factory",
            initialGameState(), viewModel.gameManager.gameState.value
        )
        assertTrue("Initial history should be empty", viewModel.gameManager.history.value.isEmpty())

        assertEquals("Initial move index should be -1", -1, viewModel.gameManager.moveIndex.value)
        assertTrue("Initial AI should be enabled", viewModel.aIEnabled.value)
        assertEquals(
            "Initial player side should be WHITE",
            CobColor.WHITE, viewModel.playerSide.value
        )
    }
}