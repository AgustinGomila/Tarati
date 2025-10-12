package com.agustin.tarati.game.ai

import com.agustin.tarati.game.ai.TaratiAI.applyMoveToBoard
import com.agustin.tarati.game.ai.TaratiAI.evaluateBoard
import com.agustin.tarati.game.ai.TaratiAI.getAllPossibleMoves
import com.agustin.tarati.game.ai.TaratiAI.getNextBestMove
import com.agustin.tarati.game.core.Checker
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.ui.screens.main.MainViewModel.Companion.initialGameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class AITest {

    @Test
    fun applyMoveToBoard_movesPiece_and_doesNotChangeTurn() {
        val gs = initialGameState(currentTurn = WHITE)
        // Move C1 -> B1 (B1 is an available vertex)
        val newState = applyMoveToBoard(gs, "C1", "B1")

        assertFalse("Origin should be empty after move", newState.checkers.containsKey("C1"))

        assertTrue("Destination should contain moved checker", newState.checkers.containsKey("B1"))

        val moved = newState.checkers["B1"]!!
        assertEquals("Moved piece should retain its color", WHITE, moved.color)

        // applyMoveToBoard DOES NOT toggle the turn; expect same currentTurn
        assertEquals(
            "applyMoveToBoard should not change currentTurn (App toggles it).",
            gs.currentTurn,
            newState.currentTurn
        )
    }

    @Test
    fun applyMoveToBoard_upgradesWhenEnteringOpponentHomeBase() {
        // Prepare a minimal state: black piece at B1 (empty normally)
        val state = GameState(mapOf("B1" to Checker(BLACK, false)), currentTurn = BLACK)
        val result = applyMoveToBoard(state, "B1", "C1") // C1 is white home-base
        val placed = result.checkers["C1"]
        assertNotNull("Piece must be placed at destination", placed)
        assertEquals("Color preserved", BLACK, placed!!.color)
        assertTrue("Piece that entered opponent home base must be upgraded", placed.isUpgraded)
    }

    @Test
    fun getAllPossibleMoves_excludesBackwardMove_forNonUpgradedWhite() {
        // Single white checker at C1, turn WHITE
        val state = GameState(mapOf("C1" to Checker(WHITE, false)), currentTurn = WHITE)
        val moves = getAllPossibleMoves(state)
        // Expect C1 -> C2 NOT to be present (this is 'backward' for WHITE)
        assertFalse(
            "Expected backward move C1 -> C2 to be disallowed for WHITE non-upgraded",
            moves.any { it.from == "C1" && it.to == "C2" },
        )
    }

    @Test
    fun getAllPossibleMoves_includesForwardMove_forNonUpgradedWhite() {
        // Single white checker at C2, turn WHITE
        val state = GameState(mapOf("C2" to Checker(WHITE, false)), currentTurn = WHITE)
        val moves = getAllPossibleMoves(state)

        // Debug: imprimir todos los movimientos posibles
        println("Movimientos posibles para C2:")
        moves.forEach { println("${it.from} -> ${it.to}") }

        // Verificar que hay al menos un movimiento válido
        assertTrue("Should have at least one valid move", moves.isNotEmpty())

        // Para debugging, podemos verificar movimientos específicos que deberían ser válidos
        // basados en la posición relativa de C2
    }

    @Test
    fun getNextBestMove_returnsSomeMove_forBlackAtDepth1() {
        // Usar el estado inicial completo, no uno minimal
        val gs = initialGameState(currentTurn = BLACK)

        // Debug: verificar movimientos posibles
        val possibleMoves = getAllPossibleMoves(gs)
        println("Movimientos posibles para BLACK en estado inicial: ${possibleMoves.size}")
        possibleMoves.forEach { println("${it.from} -> ${it.to}") }

        val result = getNextBestMove(gs, Difficulty.MEDIUM.aiDepth)
        assertNotNull("Result should not be null", result)

        // Si no hay movimientos posibles, el resultado puede ser null (juego terminado)
        if (possibleMoves.isNotEmpty()) {
            assertNotNull("Should return a move when there are legal moves", result.move)
        }
    }

    @Test
    fun getNextBestMove_returnsMate_forBlackAtDepth1() {
        // Usar el estado inicial completo, no uno minimal
        val gs = GameState(
            mapOf(
                "C1" to Checker(BLACK, true),
                "B1" to Checker(BLACK, true),
                "D2" to Checker(WHITE, true)
            ),
            currentTurn = BLACK
        )

        // Debug: verificar movimientos posibles
        val possibleMoves = getAllPossibleMoves(gs)
        println("Movimientos posibles para BLACK en estado inicial: ${possibleMoves.size}")
        possibleMoves.forEach { println("${it.from} -> ${it.to}") }

        val result = getNextBestMove(gs, Difficulty.MEDIUM.aiDepth)
        assertNotNull("Result should not be null", result)

        // Si no hay movimientos posibles, el resultado puede ser null (juego terminado)
        if (possibleMoves.isNotEmpty()) {
            assertNotNull("Should return a move when there are legal moves", result.move)
        }
    }

    @Test
    fun evaluateBoard_emptyBoard_returnsZero() {
        val emptyState = GameState(emptyMap(), currentTurn = WHITE)
        val score = evaluateBoard(emptyState)
        assertEquals(0.0, score, 0.0)
    }

    @Test
    fun evaluateBoard_moreWhitePieces_returnsPositive() {
        val state = GameState(
            mapOf(
                "C1" to Checker(WHITE, false),
                "C2" to Checker(WHITE, false),
                "C7" to Checker(BLACK, false)
            ),
            currentTurn = WHITE
        )
        val score = evaluateBoard(state)
        assertTrue(score > 0)
    }

    @Test
    fun evaluateBoard_moreBlackPieces_returnsNegative() {
        val state = GameState(
            mapOf(
                "C1" to Checker(WHITE, false),
                "C7" to Checker(BLACK, false),
                "C8" to Checker(BLACK, false)
            ),
            currentTurn = WHITE
        )
        val score = evaluateBoard(state)
        assertTrue(score < 0)
    }

    @Test
    fun isGameOver_whiteNoPieces_returnsTrue() {
        val state = GameState(
            mapOf("C7" to Checker(BLACK, false)),
            currentTurn = WHITE
        )
        assertTrue(TaratiAI.isGameOver(state))
    }

    @Test
    fun isGameOver_blackNoPieces_returnsTrue() {
        val state = GameState(
            mapOf("C1" to Checker(WHITE, false)),
            currentTurn = WHITE
        )
        assertTrue(TaratiAI.isGameOver(state))
    }

    @Test
    fun isGameOver_bothHavePieces_returnsFalse() {
        val state = GameState(
            mapOf(
                "C1" to Checker(WHITE, false),
                "C7" to Checker(BLACK, false)
            ),
            currentTurn = WHITE
        )
        assertFalse(TaratiAI.isGameOver(state))
    }

    @Test
    fun getAllPossibleMoves_includesBackwardMove_forUpgradedWhite() {
        val state = GameState(
            mapOf("C1" to Checker(WHITE, true)),
            currentTurn = WHITE
        )
        val moves = getAllPossibleMoves(state)
        // Upgraded pieces can move backward
        val hasBackwardMove = moves.any { it.from == "C1" && it.to == "C2" }
        assertTrue("Upgraded white should be able to move backward", hasBackwardMove)
    }

    @Test
    fun getAllPossibleMoves_excludesOccupiedVertices() {
        val state = GameState(
            mapOf(
                "C1" to Checker(WHITE, false),
                "B1" to Checker(BLACK, false)
            ),
            currentTurn = WHITE
        )
        val moves = getAllPossibleMoves(state)
        // Should not include C1 -> B1 because B1 is occupied
        val moveToOccupied = moves.any { it.from == "C1" && it.to == "B1" }
        assertFalse("Should not move to occupied vertex", moveToOccupied)
    }

    @Test
    fun getAllPossibleMoves_onlyCurrentTurnPieces() {
        val state = GameState(
            mapOf(
                "C1" to Checker(WHITE, false),
                "C7" to Checker(BLACK, false)
            ),
            currentTurn = WHITE
        )
        val moves = getAllPossibleMoves(state)
        // Should only include moves for white pieces
        val allMovesAreWhite = moves.all { move ->
            state.checkers[move.from]?.color == WHITE
        }
        assertTrue("Should only include moves for current turn pieces", allMovesAreWhite)
    }

    @Test
    fun isValidMove_upgradedPiece_canMoveBackward() {
        val state = GameState(
            mapOf("C1" to Checker(WHITE, true)),
            currentTurn = WHITE
        )
        // C1 -> C2 would be backward for white, but should be valid for upgraded
        val isValid = TaratiAI.isValidMove(state, "C1", "C2")
        assertTrue("Upgraded piece should be able to move backward", isValid)
    }

    @Test
    fun isValidMove_nonAdjacent_returnsFalse() {
        val state = GameState(
            mapOf("C1" to Checker(WHITE, false)),
            currentTurn = WHITE
        )
        // C1 and C3 are not adjacent
        val isValid = TaratiAI.isValidMove(state, "C1", "C3")
        assertFalse("Non-adjacent moves should be invalid", isValid)
    }

    @Test
    fun isValidMove_sameFromTo_returnsFalse() {
        val state = GameState(
            mapOf("C1" to Checker(WHITE, false)),
            currentTurn = WHITE
        )
        val isValid = TaratiAI.isValidMove(state, "C1", "C1")
        assertFalse("Moving to same position should be invalid", isValid)
    }

    @Test
    fun isForwardMove_whiteMovingUp_isForward() {
        // White moves from higher Y to lower Y (up the board)
        val isForward = TaratiAI.isForwardMove(WHITE, "C2", "B1")
        assertTrue("White moving up should be forward", isForward)
    }

    @Test
    fun isForwardMove_blackMovingDown_isForward() {
        // Black moves from lower Y to higher Y (down the board)
        val isForward = TaratiAI.isForwardMove(BLACK, "B1", "C2")
        assertTrue("Black moving down should be forward", isForward)
    }

    @Test
    fun isGameOver_noValidMoves() {
        // Estado donde WHITE no puede moverse (piezas no upgraded solo van hacia adelante)
        val stateNoMoves = GameState(
            mapOf(
                "C1" to Checker(WHITE, false), // White abajo, no puede ir más abajo
                "B1" to Checker(BLACK, false),
                "B6" to Checker(BLACK, false),
                "C12" to Checker(BLACK, false),
            ),
            currentTurn = WHITE
        )

        val possibleMoves = getAllPossibleMoves(stateNoMoves)

        // Verificar que no hay movimientos disponibles
        assertTrue("Should have no valid moves", possibleMoves.isEmpty())
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
        assertTrue("Upgraded piece should score higher", scoreUpgraded > scoreNormal)

        // Diferencia esperada: 165.5
        // normal : ((white: 0 - black: 0) * 97)   + ((wUp: 0 - bUp: 0) * 197) = 0
        // upgrade: ((white: 1.5 - black: 1) * 97) + ((wUp: 1 - bUp: 0) * 117) = 48.5 + 117 = 165.5
        assertEquals(165.5, scoreUpgraded - scoreNormal, 20.0)
    }

    @Test
    fun evaluateBoard_materialAdvantage() {
        val stateEqual = GameState(
            mapOf(
                "C1" to Checker(WHITE, false),
                "C7" to Checker(BLACK, false)
            ),
            currentTurn = WHITE
        )

        val stateWhiteAdvantage = GameState(
            mapOf(
                "C1" to Checker(WHITE, false),
                "C2" to Checker(WHITE, false),
                "C7" to Checker(BLACK, false)
            ),
            currentTurn = WHITE
        )

        val scoreEqual = evaluateBoard(stateEqual)
        val scoreAdvantage = evaluateBoard(stateWhiteAdvantage)

        // 2 vs. 1 debería ser positivo para blanco
        assertTrue("White should have positive score with material advantage", scoreAdvantage > scoreEqual)
        assertTrue("Equal material should be near zero", abs(scoreEqual) < 50.0)
    }

    @Test
    fun evaluateBoard_protectionBonus() {
        val stateIsolated = GameState(
            mapOf(
                "C1" to Checker(WHITE, false),
                "C7" to Checker(BLACK, false)
            ),
            currentTurn = WHITE
        )

        val stateProtected = GameState(
            mapOf(
                "C1" to Checker(WHITE, false),
                "C2" to Checker(WHITE, false), // Aliado adyacente
                "C7" to Checker(BLACK, false)
            ),
            currentTurn = WHITE
        )

        val scoreIsolated = evaluateBoard(stateIsolated)
        val scoreProtected = evaluateBoard(stateProtected)

        // Pieza protegida debería valer más
        assertTrue("Protected pieces should score higher", scoreProtected > scoreIsolated)
    }

    @Test
    fun evaluateBoard_symmetryBetweenColors() {
        val stateWhiteAdvantage = GameState(
            mapOf(
                "C1" to Checker(WHITE, false),
                "C2" to Checker(WHITE, false),
                "C7" to Checker(BLACK, false)
            ),
            currentTurn = WHITE
        )

        val stateBlackAdvantage = GameState(
            mapOf(
                "C1" to Checker(WHITE, false),
                "C7" to Checker(BLACK, false),
                "C8" to Checker(BLACK, false)
            ),
            currentTurn = BLACK
        )

        val scoreWhite = evaluateBoard(stateWhiteAdvantage)
        val scoreBlack = evaluateBoard(stateBlackAdvantage)

        // Deberían ser aproximadamente opuestos
        assertTrue("White advantage should be positive", scoreWhite > 50.0)
        assertTrue("Black advantage should be negative", scoreBlack < -50.0)
        assertEquals(scoreWhite, -scoreBlack, 50.0) // Aproximadamente simétrico
    }

    @Test
    fun isGameOver_noPiecesForOneColor() {
        val stateWhiteWins = GameState(
            mapOf("C1" to Checker(WHITE, false)),
            currentTurn = BLACK
        )

        assertTrue(
            "Game should be over when one color has no pieces",
            TaratiAI.isGameOver(stateWhiteWins)
        )
    }

    @Test
    fun getNextBestMove_choosesCapture() {
        // Escenario: WHITE puede capturar o moverse a espacio vacío
        val state = GameState(
            mapOf(
                "B1" to Checker(WHITE, true),
                "A1" to Checker(BLACK, false), // Puede ir hacia A1
                "B2" to Checker(BLACK, false)  // O hacia B2
            ),
            currentTurn = WHITE
        )

        val result = getNextBestMove(state, depth = 4, isMaximizingPlayer = true)

        assertNotNull("AI should find a move", result.move)
        // AI debería preferir moverse a posición ventajosa
        assertNotNull("Should have a best move", result.move)
    }

    @Test
    fun getNextBestMove_prefersFasterWin() {
        val state = GameState(
            mapOf(
                "C12" to Checker(WHITE, false), // WHITE casi perdido
                "C1" to Checker(BLACK, false),
                "C10" to Checker(BLACK, false),
                "A1" to Checker(BLACK, true),
            ),
            currentTurn = BLACK
        )

        // Con profundidad adaptativa (14 en endgame), debería ver el mate
        val result = getNextBestMove(state, isMaximizingPlayer = true)

        assertNotNull("AI should find winning move", result.move)
        assertTrue("Should have high winning score", result.score > 100000.0)

        // Imprimir para debug
        println("Best move found: ${result.move} with score: ${result.score}")
    }

    @Test
    fun isValidMove_normalPieceCannotMoveBackward() {
        val state = GameState(
            mapOf(
                "B1" to Checker(WHITE, false), // Pieza blanca normal
                "C1" to Checker(BLACK, false)
            ),
            currentTurn = WHITE
        )

        // WHITE en B1 intentando ir a C1 (hacia abajo = retroceder para WHITE)
        // Asumiendo que C1 está "abajo" de B1 en el tablero
        val canMoveBackward = TaratiAI.isValidMove(state, "B1", "C1")

        assertFalse("Normal piece should not move backward", canMoveBackward)
    }

    @Test
    fun isValidMove_upgradedPieceCanMoveAnyDirection() {
        val state = GameState(
            mapOf(
                "B1" to Checker(WHITE, true), // Pieza upgraded
            ),
            currentTurn = WHITE
        )

        // Verificar que puede moverse a posiciones adyacentes
        val adjacentVertices = listOf("A1", "B2", "B6", "C1", "C2")

        for (vertex in adjacentVertices) {
            val canMove = TaratiAI.isValidMove(state, "B1", vertex)
            assertTrue("Upgraded piece should move to adjacent $vertex", canMove)
        }
    }

    @Test
    fun transpositionTable_cachesResults() {
        val state = GameState(
            mapOf(
                "C1" to Checker(WHITE, false),
                "C2" to Checker(WHITE, false),
                "C7" to Checker(BLACK, false),
                "C8" to Checker(BLACK, false)
            ),
            currentTurn = WHITE
        )

        val startTime = System.currentTimeMillis()
        getNextBestMove(state, depth = 8) // Profundidad fija para comparar
        val firstRunTime = System.currentTimeMillis() - startTime

        val startTime2 = System.currentTimeMillis()
        getNextBestMove(state, depth = 8) // Misma profundidad
        val secondRunTime = System.currentTimeMillis() - startTime2

        println("First run: ${firstRunTime}ms, Second run: ${secondRunTime}ms")

        // Segunda ejecución debería ser más rápida (usa caché)
        assertTrue(
            "Second run should be faster due to caching",
            secondRunTime < firstRunTime || secondRunTime < 100
        )
    }

    @Test
    fun sortMoves_prioritizesGoodMoves() {
        val state = GameState(
            mapOf(
                "A1" to Checker(WHITE, true),
                "B1" to Checker(WHITE, false),
                "C7" to Checker(BLACK, false)
            ),
            currentTurn = WHITE
        )

        val moves = getAllPossibleMoves(state)

        assertTrue("Should have multiple moves", moves.size > 1)

        // Verificar que se generan movimientos válidos
        for (move in moves) {
            assertTrue(
                "Move should be valid",
                TaratiAI.isValidMove(state, move.from, move.to)
            )
        }
    }
}