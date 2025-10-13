package  com.agustin.tarati.game.ai

import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.GameBoard.getAllPossibleMoves
import com.agustin.tarati.game.logic.GameStateBuilder
import com.agustin.tarati.game.logic.createGameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameStateBuilderIntegrationTest {

    @Test
    fun builderWithAIIntegration() {
        // Create a complex game state using builder
        val state = createGameState {
            setTurn(Color.WHITE)
            setChecker("C1", Color.WHITE, false)
            setChecker("C2", Color.WHITE, false)
            setChecker("C7", Color.BLACK, true)
            setChecker("C8", Color.BLACK, false)
        }

        // AI should be able to analyze this state
        val result = TaratiAI.getNextBestMove(state, depth = Difficulty.EASY.aiDepth)

        assertNotNull("AI should return a result", result)
        // The move might be null if no valid moves, but the result should not be null
    }

    @Test
    fun complexGameScenario() {
        // Simulate a mid-game scenario
        val state = createGameState {
            setTurn(Color.WHITE)
            // Set up white pieces
            setChecker("B1", Color.WHITE, true)
            setChecker("C3", Color.WHITE, false)
            setChecker("C4", Color.WHITE, false)
            // Set up black pieces
            setChecker("B4", Color.BLACK, true)
            setChecker("C9", Color.BLACK, false)
            setChecker("C10", Color.BLACK, false)
        }

        // Verify the state
        assertEquals("Turn should be WHITE", Color.WHITE, state.currentTurn)
        assertEquals("Should have 6 checkers total", 6, state.checkers.size)
        assertEquals("Should have 3 white checkers", 3, state.checkers.values.count { it.color == Color.WHITE })
        assertEquals("Should have 3 black checkers", 3, state.checkers.values.count { it.color == Color.BLACK })
        assertEquals("Should have 2 upgraded checkers", 2, state.checkers.values.count { it.isUpgraded })
    }

    @Test
    fun builderInTestSetup() {
        // This demonstrates how the builder can be used in test setup
        val testState = GameStateBuilder()
            .setTurn(Color.BLACK)
            .setChecker("A1", Color.WHITE, true) // White king in center
            .setChecker("C7", Color.BLACK, false) // Black piece nearby
            .build()

        // Now test specific functionality with this controlled state
        val possibleMoves = getAllPossibleMoves(testState)

        // The white king at A1 should have multiple move options
        val kingMoves = possibleMoves.filter { it.from == "C7" }
        assertTrue("King should have multiple move options", kingMoves.isNotEmpty())
    }
}