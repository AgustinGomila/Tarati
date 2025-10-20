package com.agustin.tarati.game.ai

import com.agustin.tarati.game.ai.TaratiAI.applyMoveToBoard
import com.agustin.tarati.game.ai.TaratiAI.clearAIHistory
import com.agustin.tarati.game.ai.TaratiAI.getNextBestMove
import com.agustin.tarati.game.ai.TaratiAI.getRepetitionCount
import com.agustin.tarati.game.ai.TaratiAI.realGameHistory
import com.agustin.tarati.game.ai.TaratiAI.recordRealMove
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.createGameState
import com.agustin.tarati.game.core.initialGameState
import com.agustin.tarati.game.core.opponent
import com.agustin.tarati.game.logic.checkIfWouldCauseRepetition
import com.agustin.tarati.game.logic.getWinner
import com.agustin.tarati.game.logic.hashBoard
import com.agustin.tarati.game.logic.isGameOver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TripleRepetitionTest {

    @Test
    fun testTripleRepetition_WhiteLoses() {
        // Configurar una posición simple
        val gameState = createGameState {
            setTurn(WHITE)
            setCob("C1", WHITE, false)
            setCob("C7", BLACK, false)
        }

        clearAIHistory()

        // Simular triple repetición causada por las blancas
        repeat(3) {

            recordRealMove(gameState, WHITE)
        }

        // Verificar que las blancas pierden
        assertTrue("Game should be over due to triple repetition", gameState.isGameOver())
        val winner = gameState.getWinner()
        assertEquals("Black should win when white causes triple repetition", BLACK, winner)
    }

    @Test
    fun testTripleRepetition_BlackLoses() {
        val gameState = createGameState {
            setTurn(BLACK)
            setCob("C1", WHITE, false)
            setCob("C7", BLACK, false)
        }

        clearAIHistory()

        // Simular triple repetición causada por las negras
        repeat(3) {
            recordRealMove(gameState, BLACK)
        }

        assertTrue("Game should be over due to triple repetition", gameState.isGameOver())
        val winner = gameState.getWinner()
        assertEquals("White should win when black causes triple repetition", WHITE, winner)
    }


    @Test
    fun testTripleRepetition_BasicDetection() {
        clearAIHistory()

        val gameState = createGameState {
            setTurn(WHITE)
            setCob("C1", WHITE, false)
            setCob("C7", BLACK, false)
        }

        // Primera vez - no debería detectar
        val loser1 = recordRealMove(gameState, WHITE)
        assertNull("Should not detect repetition first time", loser1)
        assertFalse("Game should not be over", gameState.isGameOver())

        // Segunda vez - no debería detectar
        val loser2 = recordRealMove(gameState, WHITE)
        assertNull("Should not detect repetition second time", loser2)
        assertFalse("Game should not be over", gameState.isGameOver())

        // Tercera vez - DEBERÍA detectar
        val loser3 = recordRealMove(gameState, WHITE)
        assertEquals("Should detect triple repetition and white should lose", WHITE, loser3)
        assertTrue("Game should be over", gameState.isGameOver())
        assertEquals("Black should win", BLACK, gameState.getWinner())
    }

    @Test
    fun testTripleRepetition_DifferentStates() {
        clearAIHistory()

        val state1 = createGameState {
            setTurn(WHITE)
            setCob("C1", WHITE, false)
            setCob("C7", BLACK, false)
        }

        val state2 = createGameState {
            setTurn(WHITE)
            setCob("C2", WHITE, false) // Diferente posición
            setCob("C7", BLACK, false)
        }

        // Registrar state1 dos veces
        recordRealMove(state1, WHITE)
        recordRealMove(state1, WHITE)

        // Registrar state2 una vez - no debería activar triple repetición para state1
        val loser = recordRealMove(state2, BLACK)
        assertNull("Should not detect repetition for different state", loser)
        assertFalse("State1 should not be over", state1.isGameOver())
        assertFalse("State2 should not be over", state2.isGameOver())
    }

    @Test
    fun testTripleRepetition_CheckIfWouldCauseRepetition() {
        clearAIHistory()

        val gameState = createGameState {
            setTurn(WHITE)
            setCob("C1", WHITE, false)
            setCob("C7", BLACK, false)
        }

        // Verificar que inicialmente no causaría repetición
        assertFalse("Should not cause repetition initially", gameState.checkIfWouldCauseRepetition())

        // Registrar dos veces
        recordRealMove(gameState, WHITE)
        recordRealMove(gameState, WHITE)

        // Ahora debería causar repetición si se registra otra vez
        assertTrue("Should cause repetition after two records", gameState.checkIfWouldCauseRepetition())
    }

    @Test
    fun testTripleRepetition_ClearHistory() {
        val gameState = createGameState {
            setTurn(WHITE)
            setCob("C1", WHITE, false)
            setCob("C7", BLACK, false)
        }

        // Registrar dos veces
        recordRealMove(gameState, WHITE)
        recordRealMove(gameState, WHITE)

        // Limpiar historial
        clearAIHistory()

        // Verificar que después de limpiar, no causa repetición
        assertFalse("Should not cause repetition after clear", gameState.checkIfWouldCauseRepetition())

        val loser = recordRealMove(gameState, WHITE)
        assertNull("Should not detect repetition after clear", loser)
    }

    @Test
    fun testTripleRepetition_HashStability() {
        // Verificar que el hash es estable para la misma posición
        val state1 = createGameState {
            setTurn(WHITE)
            setCob("C1", WHITE, false)
            setCob("C7", BLACK, false)
        }

        val state2 = createGameState {
            setTurn(WHITE)
            setCob("C1", WHITE, false)
            setCob("C7", BLACK, false)
        }

        val hash1 = state1.hashBoard()
        val hash2 = state2.hashBoard()

        assertEquals("Same game states should have same hash", hash1, hash2)

        // Estado diferente debería tener hash diferente
        val state3 = createGameState {
            setTurn(BLACK) // Diferente turno
            setCob("C1", WHITE, false)
            setCob("C7", BLACK, false)
        }

        val hash3 = state3.hashBoard()
        assertFalse("Different game states should have different hashes", hash1 == hash3)
    }

    @Test
    fun testTripleRepetition_GameplaySimulation() {
        clearAIHistory()

        // Estado inicial
        var gameState = createGameState {
            setTurn(WHITE)
            setCob("C1", WHITE, false)
            setCob("B1", WHITE, false)
            setCob("C7", BLACK, false)
            setCob("B7", BLACK, false)
        }

        var moves = 0
        val maxMoves = 6 // Reducido para debug

        println("Initial state: ${gameState.hashBoard()}")

        // Simular solo 2 ciclos completos (4 movimientos)
        while (moves < maxMoves) {
            val from = if (gameState.currentTurn == WHITE) "C1" else "C7"
            val to = if (gameState.currentTurn == WHITE) "B2" else "B6"

            println("Move $moves: ${gameState.currentTurn} moves $from -> $to")

            val newState = applyMoveToBoard(gameState, from, to)
            val nextState = newState.copy(currentTurn = gameState.currentTurn.opponent())

            println("State after move: ${nextState.hashBoard()}")

            // Registrar y verificar
            val loser = recordRealMove(nextState, gameState.currentTurn)
            val currentCount = getRepetitionCount(nextState)
            println("Repetition count: $currentCount")

            if (loser != null) {
                println("Triple repetition detected at move $moves! $loser loses")
                break
            }

            gameState = nextState
            moves++
        }

        println("Finished after $moves moves")
        // No hacemos asserts aquí, solo queremos ver el output
    }

    @Test
    fun testTripleRepetition_AvoidanceByAI() {
        // Test que la IA evita movimientos que causarían triple repetición
        val gameState = createGameState {
            setTurn(WHITE)
            setCob("C1", WHITE, false)
            setCob("C7", BLACK, false)
        }

        clearAIHistory()

        // Registrar la posición 2 veces (una más causaría triple repetición)
        recordRealMove(gameState, BLACK)
        recordRealMove(gameState, BLACK)

        // La IA blanca debería evitar movimientos que lleven a esta posición
        val result = getNextBestMove(gameState, Difficulty.DEFAULT)

        assertNotNull("AI should find a move", result.move)

        // Aplicar el movimiento y verificar que no causa triple repetición
        val newState = applyMoveToBoard(gameState, result.move!!.from, result.move.to)
        val wouldCauseRepetition = newState.checkIfWouldCauseRepetition()

        assertTrue("AI should avoid moves that cause triple repetition", !wouldCauseRepetition)
    }

    @Test
    fun testTripleRepetition_WithDifferentPositions() {
        // Verificar que diferentes posiciones no activan triple repetición
        val state1 = createGameState {
            setTurn(WHITE)
            setCob("C1", WHITE, false)
            setCob("C7", BLACK, false)
        }

        val state2 = createGameState {
            setTurn(BLACK)
            setCob("C2", WHITE, false) // Diferente posición
            setCob("C7", BLACK, false)
        }

        clearAIHistory()

        // Registrar posiciones diferentes
        recordRealMove(state1, WHITE)
        recordRealMove(state2, BLACK)
        recordRealMove(state1, WHITE) // Solo segunda vez para state1

        // No debería haber triple repetición
        assertTrue("Game should not be over - different positions", !state1.isGameOver())
        assertTrue("Game should not be over - different positions", !state2.isGameOver())
    }

    @Test
    fun testTripleRepetition_ClearHistoryResets() {
        val gameState = createGameState {
            setTurn(WHITE)
            setCob("C1", WHITE, false)
            setCob("C7", BLACK, false)
        }

        // Registrar dos veces
        recordRealMove(gameState, WHITE)
        recordRealMove(gameState, WHITE)

        // Limpiar historial
        clearAIHistory()

        // Registrar de nuevo - debería empezar desde 1
        val loser = recordRealMove(gameState, WHITE)

        assertEquals("Should not detect triple repetition after clear", null, loser)
        assertTrue("Game should not be over after clear", !gameState.isGameOver())
    }

    @Test
    fun testTripleRepetition_InActualGameplay() {
        // Test más realista con gameplay actual
        var gameState = initialGameState()
        clearAIHistory()

        var repetitionDetected = false
        var moves = 0
        val maxMoves = 50

        // Jugar hasta detectar triple repetición o llegar al límite
        while (moves < maxMoves && !gameState.isGameOver()) {
            val result = getNextBestMove(gameState, Difficulty.MIN)

            if (result.move == null) break

            val newState = applyMoveToBoard(gameState, result.move.from, result.move.to)
            val nextState = newState.copy(currentTurn = gameState.currentTurn.opponent())

            // Registrar el movimiento
            val loser = recordRealMove(nextState, gameState.currentTurn)
            if (loser != null) {
                repetitionDetected = true
                println("Triple repetition detected at move $moves! $loser loses")
                break
            }

            gameState = nextState
            moves++
        }

        // En un juego real, puede que no ocurra triple repetición rápidamente,
        // pero al menos verificamos que el mecanismo funciona
        if (repetitionDetected) {
            assertTrue("Game should be over when repetition detected", gameState.isGameOver())
            val winner = gameState.getWinner()
            assertNotNull("There should be a winner when repetition occurs", winner)
        } else {
            println("No triple repetition detected in $moves moves")
        }
    }

    @Test
    fun testTripleRepetition_RealGameHistoryPersistence() {
        // Verificar que realGameHistory mantiene los registros entre llamadas
        val gameState = createGameState {
            setTurn(WHITE)
            setCob("C1", WHITE, false)
            setCob("C7", BLACK, false)
        }

        clearAIHistory()

        // Verificar que inicialmente está vacío
        assertTrue("History should be empty after clear", realGameHistory.isEmpty())

        // Primera registro
        val loser1 = recordRealMove(gameState, WHITE)
        assertNull("Should not detect repetition first time", loser1)
        assertEquals("History should have one entry", 1, realGameHistory.size)
        assertEquals("State should have count 1", 1, realGameHistory[gameState.hashBoard()])

        // Segundo registro
        val loser2 = recordRealMove(gameState, WHITE)
        assertNull("Should not detect repetition second time", loser2)
        assertEquals("History should still have one entry", 1, realGameHistory.size)
        assertEquals("State should have count 2", 2, realGameHistory[gameState.hashBoard()])

        // Tercer registro - debería detectar
        val loser3 = recordRealMove(gameState, WHITE)
        assertEquals("Should detect triple repetition", WHITE, loser3)
        assertEquals("History should still have one entry", 1, realGameHistory.size)
        assertEquals("State should have count 3", 3, realGameHistory[gameState.hashBoard()])

        // Verificar que isGameOver detecta la triple repetición
        assertTrue("Game should be over due to triple repetition", gameState.isGameOver())
    }

    @Test
    fun testTripleRepetition_GameStateConsistency() {
        // Verificar que el mismo estado produce el mismo hash
        val state1 = createGameState {
            setTurn(WHITE)
            setCob("C1", WHITE, false)
            setCob("B1", WHITE, false)
            setCob("C7", BLACK, false)
            setCob("B7", BLACK, false)
        }

        val state2 = createGameState {
            setTurn(WHITE)
            setCob("C1", WHITE, false)
            setCob("B1", WHITE, false)
            setCob("C7", BLACK, false)
            setCob("B7", BLACK, false)
        }

        val hash1 = state1.hashBoard()
        val hash2 = state2.hashBoard()

        assertEquals("Same game states should have same hash", hash1, hash2)

        clearAIHistory()

        // Registrar state1 dos veces
        recordRealMove(state1, WHITE)
        recordRealMove(state1, WHITE)

        // Verificar que state2 tiene count 2 (porque son el mismo estado)
        assertEquals("state2 should have count 2", 2, realGameHistory[hash2] ?: 0)
    }

    @Test
    fun testTripleRepetition_StepByStep() {
        val initialState = createGameState {
            setTurn(WHITE)
            setCob("C1", WHITE, false)
            setCob("B1", WHITE, false)
            setCob("C7", BLACK, false)
            setCob("B7", BLACK, false)
        }

        clearAIHistory()

        var gameState = initialState
        var moves = 0

        println("=== Step by Step Debug ===")
        println("Initial state hash: ${initialState.hashBoard()}")
        println("Initial realGameHistory size: ${realGameHistory.size}")

        // Primer movimiento: WHITE C1 -> B2
        val move1From = "C1"
        val move1To = "B2"
        val stateAfterMove1 = applyMoveToBoard(gameState, move1From, move1To)
        val stateAfterMove1WithTurn = stateAfterMove1.copy(currentTurn = BLACK)

        println("\nMove 1: WHITE $move1From -> $move1To")
        println("State after move 1 hash: ${stateAfterMove1WithTurn.hashBoard()}")

        recordRealMove(stateAfterMove1WithTurn, WHITE)
        println("realGameHistory after move 1: ${realGameHistory.size} entries")
        realGameHistory.forEach { (hash, count) ->
            println("  Hash: $hash, Count: $count")
        }

        gameState = stateAfterMove1WithTurn
        moves++

        // Segundo movimiento: BLACK C7 -> B6
        val move2From = "C7"
        val move2To = "B6"
        val stateAfterMove2 = applyMoveToBoard(gameState, move2From, move2To)
        val stateAfterMove2WithTurn = stateAfterMove2.copy(currentTurn = WHITE)

        println("\nMove 2: BLACK $move2From -> $move2To")
        println("State after move 2 hash: ${stateAfterMove2WithTurn.hashBoard()}")

        recordRealMove(stateAfterMove2WithTurn, BLACK)
        println("realGameHistory after move 2: ${realGameHistory.size} entries")
        realGameHistory.forEach { (hash, count) ->
            println("  Hash: $hash, Count: $count")
        }

        gameState = stateAfterMove2WithTurn
        moves++

        // Tercer movimiento: WHITE B2 -> C1 (volver)
        val move3From = "B2"
        val move3To = "C1"
        val stateAfterMove3 = applyMoveToBoard(gameState, move3From, move3To)
        val stateAfterMove3WithTurn = stateAfterMove3.copy(currentTurn = BLACK)

        println("\nMove 3: WHITE $move3From -> $move3To")
        println("State after move 3 hash: ${stateAfterMove3WithTurn.hashBoard()}")

        recordRealMove(stateAfterMove3WithTurn, WHITE)
        println("realGameHistory after move 3: ${realGameHistory.size} entries")
        realGameHistory.forEach { (hash, count) ->
            println("  Hash: $hash, Count: $count")
        }

        gameState = stateAfterMove3WithTurn
        moves++

        // Cuarto movimiento: BLACK B6 -> C7 (volver) - debería ser igual al estado inicial
        val move4From = "B6"
        val move4To = "C7"
        val stateAfterMove4 = applyMoveToBoard(gameState, move4From, move4To)
        val stateAfterMove4WithTurn = stateAfterMove4.copy(currentTurn = WHITE)

        println("\nMove 4: BLACK $move4From -> $move4To")
        println("State after move 4 hash: ${stateAfterMove4WithTurn.hashBoard()}")
        println("Initial state hash: ${initialState.hashBoard()}")
        println("Are they equal? ${stateAfterMove4WithTurn.hashBoard() == initialState.hashBoard()}")

        val loser4 = recordRealMove(stateAfterMove4WithTurn, BLACK)
        println("realGameHistory after move 4: ${realGameHistory.size} entries")
        realGameHistory.forEach { (hash, count) ->
            println("  Hash: $hash, Count: $count")
        }

        // Verificar si debería haber triple repetición
        val currentHash = stateAfterMove4WithTurn.hashBoard()
        val count = realGameHistory[currentHash] ?: 0
        println("Current state count: $count")

        if (loser4 != null) {
            println("Triple repetition detected at move 4! $loser4 loses")
        }

        // Verificar el estado del juego
        println("isGameOver: ${stateAfterMove4WithTurn.isGameOver()}")
        println("Winner: ${stateAfterMove4WithTurn.getWinner()}")
        println("After moves: $moves")

        // No hacemos asserts aquí - solo queremos el output para debug
    }
}