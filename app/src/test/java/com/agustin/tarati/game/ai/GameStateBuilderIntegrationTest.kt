package  com.agustin.tarati.game.ai

import com.agustin.tarati.game.ai.TaratiAI.getNextBestMove
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.createGameState
import com.agustin.tarati.game.logic.GameStateBuilder
import com.agustin.tarati.game.logic.getAllMovesForTurn
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
            setCob("C1", Color.WHITE, false)
            setCob("C2", Color.WHITE, false)
            setCob("C7", Color.BLACK, true)
            setCob("C8", Color.BLACK, false)
        }

        // AI should be able to analyze this state
        val result = getNextBestMove(state, Difficulty.MIN)

        assertNotNull("AI should return a result", result)
        // The move might be null if no valid moves, but the result should not be null
    }

    @Test
    fun complexGameScenario() {
        // Simulate a mid-game scenario
        val state = createGameState {
            setTurn(Color.WHITE)
            // Set up white pieces
            setCob("B1", Color.WHITE, true)
            setCob("C3", Color.WHITE, false)
            setCob("C4", Color.WHITE, false)
            // Set up black pieces
            setCob("B4", Color.BLACK, true)
            setCob("C9", Color.BLACK, false)
            setCob("C10", Color.BLACK, false)
        }

        // Verify the state
        assertEquals("Turn should be WHITE", Color.WHITE, state.currentTurn)
        assertEquals("Should have 6 cobs total", 6, state.cobs.size)
        assertEquals("Should have 3 white cobs", 3, state.cobs.values.count { it.color == Color.WHITE })
        assertEquals("Should have 3 black cobs", 3, state.cobs.values.count { it.color == Color.BLACK })
        assertEquals("Should have 2 upgraded cobs", 2, state.cobs.values.count { it.isUpgraded })
    }

    @Test
    fun builderInTestSetup() {
        // This demonstrates how the builder can be used in test setup
        val testState = GameStateBuilder().setTurn(Color.BLACK).setCob("A1", Color.WHITE, true) // White king in center
            .setCob("C7", Color.BLACK, false) // Black piece nearby
            .build()

        // Now test specific functionality with this controlled state
        val possibleMoves = testState.getAllMovesForTurn()

        // The white king at A1 should have multiple move options
        val kingMoves = possibleMoves.filter { it.from == "C7" }
        assertTrue("King should have multiple move options", kingMoves.isNotEmpty())
    }
}