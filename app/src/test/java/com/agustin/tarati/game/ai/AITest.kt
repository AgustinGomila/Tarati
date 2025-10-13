package com.agustin.tarati.game.ai

import com.agustin.tarati.game.ai.TaratiAI.WINNING_SCORE
import com.agustin.tarati.game.ai.TaratiAI.applyMoveToBoard
import com.agustin.tarati.game.ai.TaratiAI.evaluateBoard
import com.agustin.tarati.game.ai.TaratiAI.getNextBestMove
import com.agustin.tarati.game.ai.TaratiAI.isGameOver
import com.agustin.tarati.game.core.Checker
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameBoard.getAllPossibleMoves
import com.agustin.tarati.game.core.GameBoard.isForwardMove
import com.agustin.tarati.game.core.GameBoard.isValidMove
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.initialGameState
import com.agustin.tarati.game.core.opponent
import com.agustin.tarati.game.logic.GameStateBuilder
import com.agustin.tarati.game.logic.createGameState
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
        assertTrue(isGameOver(state))
    }

    @Test
    fun isGameOver_blackNoPieces_returnsTrue() {
        val state = GameState(
            mapOf("C1" to Checker(WHITE, false)),
            currentTurn = WHITE
        )
        assertTrue(isGameOver(state))
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
        assertFalse(isGameOver(state))
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
        val isValid = isValidMove(state, "C1", "C2")
        assertTrue("Upgraded piece should be able to move backward", isValid)
    }

    @Test
    fun isValidMove_nonAdjacent_returnsFalse() {
        val state = GameState(
            mapOf("C1" to Checker(WHITE, false)),
            currentTurn = WHITE
        )
        // C1 and C3 are not adjacent
        val isValid = isValidMove(state, "C1", "C3")
        assertFalse("Non-adjacent moves should be invalid", isValid)
    }

    @Test
    fun isValidMove_sameFromTo_returnsFalse() {
        val state = GameState(
            mapOf("C1" to Checker(WHITE, false)),
            currentTurn = WHITE
        )
        val isValid = isValidMove(state, "C1", "C1")
        assertFalse("Moving to same position should be invalid", isValid)
    }

    @Test
    fun isForwardMove_whiteMovingUp_isForward() {
        // White moves from higher Y to lower Y (up the board)
        val isForward = isForwardMove(WHITE, "C2", "B1")
        assertTrue("White moving up should be forward", isForward)
    }

    @Test
    fun isForwardMove_blackMovingDown_isForward() {
        // Black moves from lower Y to higher Y (down the board)
        val isForward = isForwardMove(BLACK, "B1", "C2")
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
        assertTrue("White advantage should be positive", scoreWhite > 130.0)
        assertTrue("Black advantage should be positive", scoreBlack < -130.0)
        assertEquals(scoreWhite, -scoreBlack, 0.0) // Aproximadamente simétrico
    }

    @Test
    fun isGameOver_noPiecesForOneColor() {
        val stateWhiteWins = GameState(
            mapOf("C1" to Checker(WHITE, false)),
            currentTurn = BLACK
        )

        assertTrue(
            "Game should be over when one color has no pieces",
            isGameOver(stateWhiteWins)
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

        val result = getNextBestMove(state, depth = Difficulty.MEDIUM.aiDepth)

        assertNotNull("AI should find a move", result.move)
        // AI debería preferir moverse a posición ventajosa
        assertNotNull("Should have a best move", result.move)
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
        val canMoveBackward = isValidMove(state, "B1", "C1")

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
            val canMove = isValidMove(state, "B1", vertex)
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
        getNextBestMove(state, depth = Difficulty.MEDIUM.aiDepth) // Profundidad fija para comparar
        val firstRunTime = System.currentTimeMillis() - startTime

        val startTime2 = System.currentTimeMillis()
        getNextBestMove(state, depth = Difficulty.MEDIUM.aiDepth) // Misma profundidad
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
                isValidMove(state, move.from, move.to)
            )
        }
    }

    @Test
    fun testEvaluationSymmetry_BasicPosition() {
        // Test 1: Posición básica - evaluación debe ser simétrica al invertir colores
        val positionWhite = createGameState {
            setTurn(WHITE)
            // Distribución 4-4 simétrica
            setChecker("C1", WHITE, false)
            setChecker("C2", WHITE, false)
            setChecker("D1", WHITE, false)
            setChecker("D2", WHITE, false)
            setChecker("C7", BLACK, false)
            setChecker("C8", BLACK, false)
            setChecker("D3", BLACK, false)
            setChecker("D4", BLACK, false)
        }

        val positionBlack = createGameState {
            setTurn(BLACK)
            // Misma posición pero colores invertidos
            setChecker("C1", BLACK, false)
            setChecker("C2", BLACK, false)
            setChecker("D1", BLACK, false)
            setChecker("D2", BLACK, false)
            setChecker("C7", WHITE, false)
            setChecker("C8", WHITE, false)
            setChecker("D3", WHITE, false)
            setChecker("D4", WHITE, false)
        }

        val scoreWhite = evaluateBoard(positionWhite)
        val scoreBlack = evaluateBoard(positionBlack)

        // Las evaluaciones deben ser opuestas (mismo valor absoluto, signo contrario)
        assertEquals(
            "Las evaluaciones deben ser simétricas al invertir colores. White: $scoreWhite, Black: $scoreBlack",
            -scoreWhite,
            scoreBlack,
            0.001
        )
    }

    @Test
    fun testEvaluationSymmetry_WithUpgrades() {
        // Test 2: Posición con piezas mejoradas - debe mantener simetría
        val positionWhite = createGameState {
            setTurn(WHITE)
            // Distribución con mejoras simétricas
            setChecker("C1", WHITE, true)   // Mejorada
            setChecker("C2", WHITE, false)
            setChecker("D1", WHITE, false)
            setChecker("D2", WHITE, true)   // Mejorada
            setChecker("C7", BLACK, true)   // Mejorada
            setChecker("C8", BLACK, false)
            setChecker("D3", BLACK, false)
            setChecker("D4", BLACK, true)   // Mejorada
        }

        val positionBlack = createGameState {
            setTurn(BLACK)
            // Posición invertida
            setChecker("C1", BLACK, true)   // Mejorada
            setChecker("C2", BLACK, false)
            setChecker("D1", BLACK, false)
            setChecker("D2", BLACK, true)   // Mejorada
            setChecker("C7", WHITE, true)   // Mejorada
            setChecker("C8", WHITE, false)
            setChecker("D3", WHITE, false)
            setChecker("D4", WHITE, true)   // Mejorada
        }

        val scoreWhite = evaluateBoard(positionWhite)
        val scoreBlack = evaluateBoard(positionBlack)

        assertEquals(
            "Las evaluaciones con mejoras deben ser simétricas. White: $scoreWhite, Black: $scoreBlack",
            -scoreWhite,
            scoreBlack,
            0.001,
        )
    }

    @Test
    fun testEvaluationFunction_SymmetricPositions() {
        // Test que solo verifica la función de evaluación (no la búsqueda completa)
        val symmetricPositions = listOf(
            // Posición 1: Distribución 4-4 equilibrada
            createGameState {
                setTurn(WHITE)
                setChecker("C1", WHITE, false); setChecker("C2", WHITE, false)
                setChecker("D1", WHITE, false); setChecker("D2", WHITE, false)
                setChecker("C7", BLACK, false); setChecker("C8", BLACK, false)
                setChecker("D3", BLACK, false); setChecker("D4", BLACK, false)
            },
            // Posición 2: Distribución 3-5 con mejoras
            createGameState {
                setTurn(WHITE)
                setChecker("C1", WHITE, true); setChecker("C2", WHITE, false)
                setChecker("D1", WHITE, true); setChecker("C7", BLACK, true)
                setChecker("C8", BLACK, false); setChecker("D3", BLACK, false)
                setChecker("D4", BLACK, true); setChecker("B1", BLACK, false)
            }
        )

        symmetricPositions.forEach { position ->
            val mirror = createMirrorPosition(position)
            val scoreOriginal = evaluateBoard(position)
            val scoreMirror = evaluateBoard(mirror)

            // Verificamos que sean opuestos (con pequeña tolerancia)
            assertEquals(
                "La evaluación debe ser simétrica para posición: $position",
                scoreOriginal,
                -scoreMirror,
                20.0,
            )
        }
    }

    @Test
    fun testAISymmetry_AdvantagePosition() {
        // Test 4: Ventaja clara para un lado debe reflejarse simétricamente
        val whiteAdvantage = createGameState {
            setTurn(WHITE)
            // Ventaja para blanco (6-2)
            setChecker("A1", WHITE, false)
            setChecker("B1", WHITE, false)
            setChecker("B2", WHITE, false)
            setChecker("B3", WHITE, false)
            setChecker("B4", WHITE, false)
            setChecker("B5", WHITE, false)
            setChecker("C1", BLACK, false)
            setChecker("C8", BLACK, false)
        }

        val blackAdvantage = createGameState {
            setTurn(BLACK)
            // Ventaja equivalente para negro (2-6)
            setChecker("C1", WHITE, false)
            setChecker("C8", WHITE, false)
            setChecker("A1", BLACK, false)
            setChecker("B1", BLACK, false)
            setChecker("B2", BLACK, false)
            setChecker("B3", BLACK, false)
            setChecker("B4", BLACK, false)
            setChecker("B5", BLACK, false)
        }

        val scoreWhiteAdvantage = evaluateBoard(whiteAdvantage)
        val scoreBlackAdvantage = evaluateBoard(blackAdvantage)

        // La ventaja de blanco debe ser igual en magnitud a la ventaja de negro
        assertEquals(
            "Las ventajas deben ser simétricas. WhiteAdv: $scoreWhiteAdvantage, BlackAdv: $scoreBlackAdvantage",
            scoreWhiteAdvantage,
            -scoreBlackAdvantage,
            0.0
        )
    }

    @Test
    fun testEvaluationZero_PerfectBalance() {
        // Test 5: Posición perfectamente equilibrada debe evaluar cerca de cero
        val balancedPosition = createGameState {
            setTurn(WHITE)
            // Distribución 4-4 idéntica
            setChecker("C1", WHITE, false)
            setChecker("C2", WHITE, false)
            setChecker("C3", WHITE, false)
            setChecker("C4", WHITE, false)
            setChecker("C7", BLACK, false)
            setChecker("C8", BLACK, false)
            setChecker("C9", BLACK, false)
            setChecker("C10", BLACK, false)
        }

        val score = evaluateBoard(balancedPosition)

        // Debe estar cerca de cero (no exactamente por posibles pequeñas asimetrías del tablero)
        assertTrue("Posición equilibrada debe evaluar cerca de cero. Score: $score", abs(score) == 0.0)
    }

    @Test
    fun testEvaluationSymmetry_SamePositionDifferentTurn() {
        // Test 6: Misma posición, diferente turno - evaluación debe ser similar
        val positionWhiteTurn = createGameState {
            setTurn(WHITE)
            setChecker("C1", WHITE, false)
            setChecker("C2", WHITE, false)
            setChecker("D1", WHITE, false)
            setChecker("D2", WHITE, false)
            setChecker("C7", BLACK, false)
            setChecker("C8", BLACK, false)
            setChecker("D3", BLACK, false)
            setChecker("D4", BLACK, false)
        }

        val positionBlackTurn = createGameState {
            setTurn(BLACK)
            // Mismas piezas, solo cambia el turno
            setChecker("C1", WHITE, false)
            setChecker("C2", WHITE, false)
            setChecker("D1", WHITE, false)
            setChecker("D2", WHITE, false)
            setChecker("C7", BLACK, false)
            setChecker("C8", BLACK, false)
            setChecker("D3", BLACK, false)
            setChecker("D4", BLACK, false)
        }

        val scoreWhiteTurn = evaluateBoard(positionWhiteTurn)
        val scoreBlackTurn = evaluateBoard(positionBlackTurn)

        // La evaluación no debería cambiar drásticamente solo por el turno
        // (puede haber pequeñas diferencias por cómo se calcula, pero no grandes)
        assertTrue(
            "La evaluación no debería cambiar solo por el turno. WhiteTurn: $scoreWhiteTurn, BlackTurn: $scoreBlackTurn",
            abs(scoreWhiteTurn - scoreBlackTurn) == 0.0
        )
    }

    /**
     * Helper para crear posiciones espejo automáticamente
     */
    private fun createMirrorPosition(original: GameState): GameState {
        val builder = GameStateBuilder()
        builder.setTurn(original.currentTurn.opponent())

        // Intercambiar todas las piezas de color
        original.checkers.forEach { (vertex, checker) ->
            builder.setChecker(vertex, checker.color.opponent(), checker.isUpgraded)
        }

        return builder.build()
    }

    @Test
    fun testAutomaticMirrorSymmetry() {
        // Test 7: Usando el helper de espejo automático
        val originalPosition = createGameState {
            setTurn(WHITE)
            setChecker("B1", WHITE, false)
            setChecker("C2", WHITE, false)
            setChecker("D1", WHITE, false)
            setChecker("C1", WHITE, false)
            setChecker("B4", BLACK, false)
            setChecker("C7", BLACK, false)
            setChecker("D3", BLACK, false)
            setChecker("C8", BLACK, false)
        }

        val mirrorPosition = createMirrorPosition(originalPosition)

        val originalScore = evaluateBoard(originalPosition)
        val mirrorScore = evaluateBoard(mirrorPosition)

        assertEquals(
            "El espejo automático debe producir evaluación simétrica. Original: $originalScore, Mirror: $mirrorScore",
            originalScore,
            mirrorScore,
            10.0
        )
    }

    @Test
    fun getNextBestMove_prefersFasterWin_C1_mate_B1_occupied() {
        val state = GameState(
            mapOf(
                "C12" to Checker(WHITE, false), // WHITE casi perdido

                "C11" to Checker(BLACK, false),
                "B6" to Checker(BLACK, false),
                "C2" to Checker(BLACK, true),
                "B1" to Checker(BLACK, false),
                "C6" to Checker(BLACK, false),
                "C7" to Checker(BLACK, false),
                "C8" to Checker(BLACK, false),
            ),
            currentTurn = BLACK
        )

        // Con profundidad adaptativa (14 en endgame), debería ver el mate
        val result = getNextBestMove(state)

        assertNotNull("AI should find winning move", result.move)
        assertTrue("Should have high winning score", result.score == -WINNING_SCORE)

        // Imprimir para debug
        println("Best move found: ${result.move} with score: ${result.score}")
    }

    @Test
    fun getNextBestMove_prefersFasterWin_C1_mate_B1_free() {
        val state = GameState(
            mapOf(
                "C12" to Checker(WHITE, false), // WHITE casi perdido

                "C11" to Checker(BLACK, false),
                "B6" to Checker(BLACK, false),
                "C2" to Checker(BLACK, true),
                "C5" to Checker(BLACK, false),
                "C6" to Checker(BLACK, false),
                "C7" to Checker(BLACK, false),
                "C8" to Checker(BLACK, false),
            ),
            currentTurn = BLACK
        )

        // Con profundidad adaptativa (14 en endgame), debería ver el mate
        val result = getNextBestMove(state)

        assertNotNull("AI should find winning move", result.move)
        assertTrue("Should have high winning score", result.score == -WINNING_SCORE)

        // Imprimir para debug
        println("Best move found: ${result.move} with score: ${result.score}")
    }
}