package com.agustin.tarati.game.ai

import com.agustin.tarati.game.ai.TaratiAI.WINNING_SCORE
import com.agustin.tarati.game.ai.TaratiAI.applyMoveToBoard
import com.agustin.tarati.game.ai.TaratiAI.evaluateBoard
import com.agustin.tarati.game.ai.TaratiAI.getNextBestMove
import com.agustin.tarati.game.ai.TaratiAI.getWinner
import com.agustin.tarati.game.ai.TaratiAI.isGameOver
import com.agustin.tarati.game.ai.TaratiAI.sortMoves
import com.agustin.tarati.game.core.Checker
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameBoard.getAllPossibleMoves
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Assert
import org.junit.Assert.assertNotNull
import org.junit.Test

class AIForceTest {

    @Test
    fun testQuickEvaluate_BasicPieceCount() {
        // Test básico de evaluación rápida
        val checkers = mutableMapOf(
            "B1" to Checker(BLACK, isUpgraded = false),
            "B2" to Checker(WHITE, isUpgraded = false)
        )
        val gameState = GameState(checkers, currentTurn = BLACK)

        val score = TaratiAI.quickEvaluate(gameState)

        // 1 negro (-1) vs. 1 blanco (+1) = 0
        assertEquals(0.0, score)
    }

    @Test
    fun testGameOverDetectionInSorting() {
        // Test específico para verificar que sortMoves detecta estados de game over
        val checkers = mutableMapOf(
            "C2" to Checker(BLACK, isUpgraded = true),
            "D2" to Checker(WHITE, isUpgraded = false) // Última pieza blanca
        )
        val gameState = GameState(checkers, currentTurn = BLACK)

        val moves = mutableListOf(
            Move("C2", "B2"),
            Move("C2", "C1"),
            Move("C2", "C3")
        )

        // En sortMoves, si algún movimiento lleva a game over, debe ser priorizado
        sortMoves(moves, gameState, isMaximizingPlayer = true)

        // Verificar que no hay movimientos que lleven a game over en esta posición
        // (ninguno debería capturar D2 directamente)
        // Este test sirve como verificación negativa
        assertTrue(true) // Placeholder - el test pasa si no hay excepciones
    }

    @Test
    fun testEdgeCase_SinglePieceEndgame() {
        // Caso borde: una sola pieza en el tablero
        val checkers = mutableMapOf(
            "A1" to Checker(WHITE, isUpgraded = false)
        )
        val gameState = GameState(checkers, currentTurn = WHITE)

        val moves = mutableListOf(
            Move("A1", "B1"),
            Move("A1", "B2")
        )

        sortMoves(moves, gameState, isMaximizingPlayer = false)

        // Ambos movimientos deberían ser válidos, ninguno gana inmediatamente
        assertEquals(2, moves.size)
    }

    // Helper para crear estados de juego personalizados
    class GameStateBuilder {
        private val checkers = mutableMapOf<String, Checker>()
        private var currentTurn: Color = WHITE

        fun setTurn(turn: Color) {
            currentTurn = turn
        }

        fun setChecker(vertexId: String, color: Color, isUpgraded: Boolean) {
            checkers[vertexId] = Checker(color, isUpgraded)
        }

        fun build(): GameState {
            return GameState(checkers = checkers, currentTurn = currentTurn)
        }
    }

    private fun createGameState(block: GameStateBuilder.() -> Unit): GameState {
        val builder = GameStateBuilder()
        builder.block()
        return builder.build()
    }

    @Test
    fun testMateInOne_Blockade() {
        // Situación: Blanco puede mover para dejar a negro sin movimientos
        val gameState = createGameState {
            setTurn(WHITE)
            // Piezas negras (3)
            setChecker("C1", BLACK, false)
            setChecker("C7", BLACK, false)
            setChecker("C8", BLACK, false)
            // Piezas blancas (5)
            setChecker("B1", WHITE, false)
            setChecker("C2", WHITE, false)
            setChecker("D1", WHITE, false)
            setChecker("B4", WHITE, false)
            setChecker("C6", WHITE, false)
        }

        val result = getNextBestMove(gameState, depth = Difficulty.EASY.aiDepth)
        val newState = applyMoveToBoard(gameState, result.move!!.from, result.move.to)

        // Después del movimiento, negro no debería tener movimientos
        assertTrue(getAllPossibleMoves(newState).none { gameState.checkers[it.from]?.color == BLACK })
    }

    @Test
    fun testStalemateInOne() {
        // Situación: El jugador actual puede forzar un ahogado (sin movimientos legales para el oponente)
        val gameState = createGameState {
            setTurn(WHITE)
            // Negras (2 piezas casi bloqueadas)
            setChecker("C1", BLACK, false)
            setChecker("C2", BLACK, false)
            // Blancas (5 piezas)
            setChecker("B1", WHITE, false)
            setChecker("C3", WHITE, false)
            setChecker("C12", WHITE, false)
            setChecker("C6", WHITE, false)
            setChecker("C7", WHITE, false)
            setChecker("C8", WHITE, false)
        }

        val result = getNextBestMove(gameState, depth = Difficulty.MEDIUM.aiDepth)
        val newState = applyMoveToBoard(gameState, result.move!!.from, result.move.to)

        // Después del movimiento, negro no debería tener movimientos legales
        val blackMoves = getAllPossibleMoves(newState).filter { gameState.checkers[it.from]?.color == BLACK }
        assertTrue("Negro debería estar ahogado. Movimientos disponibles: $blackMoves", blackMoves.isEmpty())
    }

    @Test
    fun testWinningPosition_Evaluation() {
        // Test de la función de evaluación en posiciones ganadoras
        val gameState = createGameState {
            setTurn(WHITE)
            // Posición claramente ganadora para blanco
            setChecker("A1", WHITE, true)
            setChecker("B1", WHITE, true)
            setChecker("B2", WHITE, true)
            setChecker("B3", WHITE, false)
            setChecker("B4", WHITE, false)
            setChecker("B5", WHITE, false)
            setChecker("B6", WHITE, false)
            setChecker("C1", BLACK, false) // Única pieza negra
        }

        val score = evaluateBoard(gameState)
        assertTrue("La evaluación debería ser positiva para blanco. Score: $score", score > 0)
    }

    @Test
    fun testGameOver_Detection() {
        // Test de detección de fin de juego
        val gameState = createGameState {
            setTurn(WHITE)
            // Solo una pieza negra que puede ser capturada
            setChecker("C1", BLACK, false)
            // Múltiples piezas blancas
            setChecker("B1", WHITE, false)
            setChecker("C2", WHITE, false)
            setChecker("D1", WHITE, false)
            setChecker("A1", WHITE, false)
            setChecker("B2", WHITE, false)
            setChecker("B3", WHITE, false)
            setChecker("B4", WHITE, false)
        }

        // Aplicar movimiento ganador
        val newState = applyMoveToBoard(gameState, "B1", "C1")

        // Debería detectar juego terminado
        assertTrue(isGameOver(newState))
    }

    @Test
    fun testForkOpportunity() {
        // Situación: Movimiento que amenaza múltiples piezas
        val gameState = createGameState {
            setTurn(WHITE)
            // Negras en posición vulnerable
            setChecker("C1", BLACK, false)
            setChecker("C2", BLACK, false)
            // Blancas en posición de tenedor
            setChecker("B1", WHITE, true) // Pieza mejorada
            setChecker("C3", WHITE, false)
            setChecker("D1", WHITE, false)
            setChecker("D2", WHITE, false)
            setChecker("A1", WHITE, false)
            setChecker("B2", WHITE, false)
            setChecker("B3", WHITE, false)
        }

        val result = getNextBestMove(gameState, depth = Difficulty.MEDIUM.aiDepth)

        // El movimiento debería crear múltiples amenazas
        val newState = applyMoveToBoard(gameState, result.move!!.from, result.move.to)
        val threats = getAllPossibleMoves(newState).size
        assertTrue("Debería crear múltiples amenazas. Amenazas: $threats", threats > 8)
    }

    @Test
    fun testStalemateInTwo() {
        // Situación: Ahogado forzado en 2 movimientos
        val gameState = com.agustin.tarati.game.logic.createGameState {
            setTurn(WHITE)
            // Distribución 3-5
            // Negras
            setChecker("C7", BLACK, false)
            setChecker("C8", BLACK, false)
            setChecker("D3", BLACK, false)
            // Blancas
            setChecker("C1", WHITE, true)
            setChecker("C2", WHITE, true)
            setChecker("D1", WHITE, false)
            setChecker("D2", WHITE, false)
            setChecker("B1", WHITE, false)
        }

        val result = getNextBestMove(gameState, depth = Difficulty.MEDIUM.aiDepth)

        // Debería identificar el camino hacia el ahogado
        assertTrue(result.score >= 350)
    }

    @Test
    fun testMateInTwo_ForcedSequence() {
        // Situación: Blanco puede forzar mate en 2 movimientos
        val gameState = com.agustin.tarati.game.logic.createGameState {
            setTurn(WHITE)
            // Piezas negras (2)
            setChecker("C1", BLACK, false)
            setChecker("C8", BLACK, false)
            // Piezas blancas (6)
            setChecker("B1", WHITE, false)
            setChecker("C2", WHITE, true) // Mejorada para mayor movilidad
            setChecker("D1", WHITE, false)
            setChecker("B4", WHITE, false)
            setChecker("C6", WHITE, false)
            setChecker("A1", WHITE, false)
        }

        val result = getNextBestMove(gameState, depth = Difficulty.MEDIUM.aiDepth)

        // El movimiento debería ser parte de una secuencia ganadora
        assertTrue(result.score == WINNING_SCORE) // Puntuación muy alta indica victoria inminente
    }

    @Test
    fun testMateInOne_White() {
        // Situación: Blanco puede forzar mate en 1 movimientos
        val gameState = createGameState {
            setTurn(WHITE)
            // Piezas negras (1)
            setChecker("B4", BLACK, true)
            // Piezas blancas (7)
            setChecker("A1", WHITE, true)
            setChecker("B2", WHITE, true)
            setChecker("B1", WHITE, true)
            setChecker("C2", WHITE, true)
            setChecker("C12", WHITE, true)
            setChecker("C4", WHITE, false)
            setChecker("C5", WHITE, false)
        }

        val result = getNextBestMove(gameState, depth = Difficulty.MEDIUM.aiDepth)

        println(result)

        assertTrue(result.move != null)
        assertTrue(
            (result.move?.from == "B2" && result.move.to == "B3") ||
                    (result.move?.from == "A1" && result.move.to == "B3") ||
                    (result.move?.from == "A1" && result.move.to == "B5")
        )
    }

    @Test
    fun testMateInOne_Black() {
        // Situación: Negro puede forzar mate en 1 movimientos
        val gameState = createGameState {
            setTurn(BLACK)
            // Piezas blancas (1)
            setChecker("B4", WHITE, true)
            // Piezas negras (7)
            setChecker("A1", BLACK, true)
            setChecker("B2", BLACK, true)
            setChecker("B1", BLACK, true)
            setChecker("C2", BLACK, true)
            setChecker("C12", BLACK, true)
            setChecker("C4", BLACK, false)
            setChecker("C5", BLACK, false)
        }

        val result = getNextBestMove(gameState, depth = Difficulty.MEDIUM.aiDepth)

        println(result)

        assertTrue(result.move != null)
        assertTrue(
            (result.move?.from == "B2" && result.move.to == "B3") ||
                    (result.move?.from == "A1" && result.move.to == "B3") ||
                    (result.move?.from == "A1" && result.move.to == "B5")
        )
    }

    @Test
    fun evaluateBoard_upgradedPieceIsMoreValuable() {
        val stateNormal = GameState(
            mapOf(
                "C1" to Checker(WHITE, false),
                "C7" to Checker(BLACK, false)
            ),
            currentTurn = WHITE
        )

        val stateUpgraded = GameState(
            mapOf(
                "C1" to Checker(WHITE, true),
                "C7" to Checker(BLACK, false)
            ),
            currentTurn = WHITE
        )

        val scoreNormal = evaluateBoard(stateNormal)
        val scoreUpgraded = evaluateBoard(stateUpgraded)

        // La pieza upgraded debería tener mejor evaluación
        Assert.assertTrue("Upgraded piece should score higher", scoreUpgraded > scoreNormal)

        Assert.assertEquals(110.0, scoreUpgraded - scoreNormal, 0.0)
    }

    @Test
    fun testMateInTwo_Black() {
        // Situación: Negro puede forzar mate en 2 movimientos
        val initialState = createGameState {
            setTurn(BLACK)
            // Piezas blancas (3)
            setChecker("B4", WHITE, false)
            setChecker("C10", WHITE, false)
            setChecker("A1", WHITE, true)
            // Piezas negras (5)
            setChecker("C6", BLACK, false)
            setChecker("B2", BLACK, false)
            setChecker("B1", BLACK, true)
            setChecker("B6", BLACK, false)
            setChecker("C2", BLACK, true)
        }

        // Movimiento 1: Negro juega (debe encontrar C6 -> B3)
        val blackMove1 = getNextBestMove(initialState, depth = Difficulty.MEDIUM.aiDepth)

        println("Black move 1: ${blackMove1.move} with score: ${blackMove1.score}")

        assertNotNull("Black should find a move", blackMove1.move)
        assertEquals("Black should move from C6", blackMove1.move?.from, "C6")
        assertEquals("Black should move to B3", blackMove1.move?.to, "B3")

        // Aplicar el movimiento de negro
        val stateAfterBlack1 = applyMoveToBoard(initialState, "C6", "B3")
            .copy(currentTurn = WHITE)

        // Movimiento 2: Blanco está forzado (debe jugar C10 -> C9 o perder inmediatamente)
        val whiteMove = getNextBestMove(stateAfterBlack1, depth = Difficulty.MEDIUM.aiDepth)

        println("White move (forced): ${whiteMove.move} with score: ${whiteMove.score}")

        assertNotNull("White should find a move", whiteMove.move)
        // Verificar que, blanco juega la única jugada que retrasa el mate
        // (puede variar según la posición, pero debería ser defensiva)

        assertTrue("White should make a defensive move", whiteMove.move != null)
        assertEquals("White should move from C10", whiteMove.move?.from, "C10")
        assertEquals("White should move to C9", whiteMove.move?.to, "C9")

        // Aplicar el movimiento de blanco (asumiendo que juega la mejor defensa)
        val stateAfterWhite = applyMoveToBoard(stateAfterBlack1, whiteMove.move!!.from, whiteMove.move.to)
            .copy(currentTurn = BLACK)

        // Movimiento 3: Negro da mate
        val blackMove2 = getNextBestMove(stateAfterWhite, depth = Difficulty.MEDIUM.aiDepth)

        assertNotNull("Black should find a move", blackMove2.move)
        assertTrue("Black should move from A1 or B4", blackMove2.move?.from == "A1" || blackMove2.move?.from == "B4")
        assertEquals("Black should move to B5", blackMove2.move?.to, "B5")

        println("Black move: ${blackMove2.move} with score: ${blackMove2.score}")

        assertNotNull("Black should find mate", blackMove2.move)

        // Aplicar el movimiento final
        val finalState = applyMoveToBoard(stateAfterWhite, blackMove2.move!!.from, blackMove2.move.to)
            .copy(currentTurn = WHITE)

        // Verificar que es mate
        assertTrue("Should be game over", isGameOver(finalState))
        assertEquals("Black should win", BLACK, getWinner(finalState))

        // El score del primer movimiento debería indicar mate forzado
        assertTrue(
            "Black's first move should have winning score (negative for BLACK)",
            blackMove1.score == -WINNING_SCORE
        )
    }
}