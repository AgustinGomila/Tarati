package com.agustin.tarati.ui.screens.main

import com.agustin.tarati.game.ai.Difficulty
import com.agustin.tarati.game.core.Checker
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.core.cleanGameState
import com.agustin.tarati.game.core.initialGameState
import com.agustin.tarati.ui.screens.settings.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.dsl.module

class MainViewModelTest {

    private lateinit var viewModel: MainViewModel
    private lateinit var mockSettingsRepository: SettingsRepository

    @Before
    fun setUp() {
        mockSettingsRepository = mockk()
        startKoin {
            modules(module {
                single { mockSettingsRepository }
            })
        }
        viewModel = MainViewModel().apply {
            sr = mockSettingsRepository
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun initialGameState_hasCorrectSetup() {
        val gameState = initialGameState()

        assertEquals("Initial turn should be WHITE", Color.WHITE, gameState.currentTurn)
        assertEquals("Should have 8 checkers total", 8, gameState.checkers.size)
        assertEquals(
            "Should have 4 white checkers",
            4,
            gameState.checkers.values.count { it.color == Color.WHITE })
        assertEquals(
            "Should have 4 black checkers",
            4,
            gameState.checkers.values.count { it.color == Color.BLACK })

        // Verify specific positions
        assertTrue("C1 should have white checker", gameState.checkers["C1"]?.color == Color.WHITE)
        assertTrue("C7 should have black checker", gameState.checkers["C7"]?.color == Color.BLACK)
        assertTrue("D1 should have white checker", gameState.checkers["D1"]?.color == Color.WHITE)
        assertTrue("D3 should have black checker", gameState.checkers["D3"]?.color == Color.BLACK)
    }

    @Test
    fun cleanGameState_hasNoCheckers() {
        val gameState = cleanGameState()

        assertTrue("Clean state should have no checkers", gameState.checkers.isEmpty())
        assertEquals("Turn should be WHITE by default", Color.WHITE, gameState.currentTurn)
    }

    @Test
    fun cleanGameState_withCustomTurn() {
        val gameState = cleanGameState(Color.BLACK)

        assertTrue("Clean state should have no checkers", gameState.checkers.isEmpty())
        assertEquals("Turn should be BLACK", Color.BLACK, gameState.currentTurn)
    }

    @Test
    fun updateGameState_changesState() {
        val newState = GameState(
            checkers = mapOf("A1" to Checker(Color.WHITE, true)),
            currentTurn = Color.BLACK
        )

        viewModel.updateGameState(newState)

        assertEquals("Game state should be updated", newState, viewModel.gameState.value)
    }

    @Test
    fun updateHistory_changesHistory() {
        val move = Move("C1", "B1")
        val gameState = initialGameState()
        val history = listOf(Pair(move, gameState))

        viewModel.updateHistory(history)

        assertEquals("History should be updated", history, viewModel.history.value)
    }

    @Test
    fun updateDifficulty_changesDifficultyAndSaves() = runTest {
        val newDifficulty = Difficulty.HARD

        coEvery { mockSettingsRepository.setDifficulty(newDifficulty) } returns Unit

        viewModel.updateDifficulty(newDifficulty)

        assertEquals("Difficulty should be updated", newDifficulty, viewModel.difficulty.value)
        coVerify { mockSettingsRepository.setDifficulty(newDifficulty) }
    }

    @Test
    fun updateMoveIndex_changesIndex() {
        viewModel.updateMoveIndex(5)

        assertEquals("Move index should be 5", 5, viewModel.moveIndex.value)
    }

    @Test
    fun incrementMoveIndex_increasesByOne() {
        viewModel.updateMoveIndex(2)
        viewModel.incrementMoveIndex()

        assertEquals("Move index should be 3", 3, viewModel.moveIndex.value)
    }

    @Test
    fun decrementMoveIndex_decreasesByOne() {
        viewModel.updateMoveIndex(2)
        viewModel.decrementMoveIndex()

        assertEquals("Move index should be 1", 1, viewModel.moveIndex.value)
    }

    @Test
    fun updateAIEnabled_changesAIState() {
        viewModel.updateAIEnabled(false)

        assertFalse("AI should be disabled", viewModel.aIEnabled.value)
    }

    @Test
    fun updatePlayerSide_changesPlayerSide() {
        viewModel.updatePlayerSide(Color.BLACK)

        assertEquals("Player side should be BLACK", Color.BLACK, viewModel.playerSide.value)
    }

    @Test
    fun initialState_hasDefaultValues() {
        assertEquals(
            "Initial game state should match factory",
            initialGameState(), viewModel.gameState.value
        )
        assertTrue("Initial history should be empty", viewModel.history.value.isEmpty())
        assertEquals(
            "Initial difficulty should be DEFAULT",
            Difficulty.DEFAULT, viewModel.difficulty.value
        )
        assertEquals("Initial move index should be -1", -1, viewModel.moveIndex.value)
        assertTrue("Initial AI should be enabled", viewModel.aIEnabled.value)
        assertEquals(
            "Initial player side should be WHITE",
            Color.WHITE, viewModel.playerSide.value
        )
    }
}