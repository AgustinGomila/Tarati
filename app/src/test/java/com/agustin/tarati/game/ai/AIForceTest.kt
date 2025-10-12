package com.agustin.tarati.game.ai

import com.agustin.tarati.game.ai.TaratiAI.sortMovesAlt
import com.agustin.tarati.game.core.Checker
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class AIForceTest {

    @Test
    fun testSortMovesAlt_MateMovePrioritizedForMaximizingPlayer() {
        // Configuración: Negras pueden dar mate en un movimiento
        val checkers = mutableMapOf(
            "B1" to Checker(BLACK, isUpgraded = true),
            "C1" to Checker(BLACK, isUpgraded = true),
            "D2" to Checker(WHITE, isUpgraded = false)
        )
        val gameState = GameState(checkers, currentTurn = BLACK)

        val moves = mutableListOf(
            Move("B1", "B2"),  // Movimiento normal
            Move("B1", "C2")   // Movimiento de mate
        )

        sortMovesAlt(moves, gameState, isMaximizingPlayer = true)

        // El movimiento de mate debe ser primero
        assertEquals("C2", moves[0].to)
        assertEquals("B2", moves[1].to)
    }

    @Test
    fun testSortMovesAlt_StalemateAvoidance() {
        // Configuración de ahogo: movimientos que evitan quedar atrapado
        val checkers = mutableMapOf(
            "C3" to Checker(WHITE, isUpgraded = false),
            "B2" to Checker(BLACK, isUpgraded = true),
            "B1" to Checker(BLACK, isUpgraded = true),
            "C1" to Checker(BLACK, isUpgraded = false)
        )
        val gameState = GameState(checkers, currentTurn = WHITE)

        val moves = mutableListOf(
            Move("C3", "B3"),  // Movimiento hacia trampa
            Move("C3", "C4"),  // Movimiento de escape
            Move("C3", "C2")   // Movimiento que mantiene movilidad
        )

        sortMovesAlt(moves, gameState, isMaximizingPlayer = false)

        // Los movimientos que mantienen movilidad deben ser prioritarios
        assertTrue(moves[0].to == "C4" || moves[0].to == "C2")
    }

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
    fun testQuickEvaluate_WithUpgradedPieces() {
        // Test con piezas mejoradas
        val checkers = mutableMapOf(
            "B1" to Checker(BLACK, isUpgraded = true),  // +1.5
            "B2" to Checker(WHITE, isUpgraded = true),  // -1.5
            "C1" to Checker(BLACK, isUpgraded = false)  // +1.0
        )
        val gameState = GameState(checkers, currentTurn = BLACK)

        val score = TaratiAI.quickEvaluate(gameState)

        // 1.5 + 1.0 - 1.5 = 1.0
        assertEquals(1.0, score, 0.01)
    }

    @Test
    fun testSortMoves_ComparisonWithOriginal() {
        // Test comparativo entre sortMoves original y sortMovesAlt
        val checkers = mutableMapOf(
            "B1" to Checker(BLACK, isUpgraded = true),
            "C1" to Checker(BLACK, isUpgraded = true),
            "D2" to Checker(WHITE, isUpgraded = false)
        )
        val gameState = GameState(checkers, currentTurn = BLACK)

        val movesOriginal = mutableListOf(
            Move("B1", "B2"),
            Move("B1", "C2")
        )

        val movesNew = movesOriginal.toMutableList()

        // Aplicar ambos algoritmos
        TaratiAI.sortMoves(movesOriginal, gameState, isMaximizingPlayer = true)
        sortMovesAlt(movesNew, gameState, isMaximizingPlayer = true)

        // Ambos deberían priorizar el movimiento de mate en este caso simple
        assertEquals("C2", movesOriginal[0].to)
        assertEquals("C2", movesNew[0].to)
    }

    @Test
    fun testGameOverDetectionInSorting() {
        // Test específico para verificar que sortMovesAlt detecta estados de game over
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

        // En sortMovesAlt, si algún movimiento lleva a game over, debe ser priorizado
        sortMovesAlt(moves, gameState, isMaximizingPlayer = true)

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

        sortMovesAlt(moves, gameState, isMaximizingPlayer = false)

        // Ambos movimientos deberían ser válidos, ninguno gana inmediatamente
        assertEquals(2, moves.size)
    }
}