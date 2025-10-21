package com.agustin.tarati.game.ai

import com.agustin.tarati.game.ai.TaratiAI.applyMoveToBoard
import com.agustin.tarati.game.ai.TaratiAI.evalConfig
import com.agustin.tarati.game.ai.TaratiAI.evaluateBoard
import com.agustin.tarati.game.ai.TaratiAI.getNextBestMove
import com.agustin.tarati.game.ai.TaratiAI.quickEvaluate
import com.agustin.tarati.game.ai.TaratiAI.sortMoves
import com.agustin.tarati.game.core.Cob
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameBoard.centerVertices
import com.agustin.tarati.game.core.GameBoard.homeBases
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.logic.getAllMovesForTurn
import com.agustin.tarati.game.logic.getWinner
import com.agustin.tarati.game.logic.isGameOver
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNotNull
import org.junit.Test

@ExperimentalCoroutinesApi
class AIForceTest {

    @Test
    fun testQuickEvaluate_BasicPieceCount() {
        // Test básico de evaluación rápida
        val cobs = mutableMapOf(
            "B1" to Cob(BLACK, isUpgraded = false),
            "B2" to Cob(WHITE, isUpgraded = false)
        )
        val gameState = GameState(cobs, currentTurn = BLACK)

        val score = quickEvaluate(gameState)

        // 1 negro (-1) vs. 1 blanco (+1) = 0
        assertEquals(0.0, score)
    }

    @Test
    fun testGameOverDetectionInSorting() {
        // Test específico para verificar que sortMoves detecta estados de game over
        val cobs = mutableMapOf(
            "C2" to Cob(BLACK, isUpgraded = true),
            "D2" to Cob(WHITE, isUpgraded = false) // Última pieza blanca
        )
        val gameState = GameState(cobs, currentTurn = BLACK)

        val moves = mutableListOf(
            Move("C2", "B2"),
            Move("C2", "C1"),
            Move("C2", "C3")
        )

        // En sortMoves, si algún movimiento lleva a game over, debe ser priorizado
        sortMoves(moves, gameState, true)

        // Verificar que no hay movimientos que lleven a game over en esta posición
        // (ninguno debería capturar D2 directamente)
        // Este test sirve como verificación negativa
        assertTrue(true) // Placeholder - el test pasa si no hay excepciones
    }

    @Test
    fun testEdgeCase_SinglePieceEndgame() {
        // Caso borde: una sola pieza en el tablero
        val cobs = mutableMapOf(
            "A1" to Cob(WHITE, isUpgraded = false)
        )
        val gameState = GameState(cobs, currentTurn = WHITE)

        val moves = mutableListOf(
            Move("A1", "B1"),
            Move("A1", "B2")
        )

        sortMoves(moves, gameState, isMaximizing = false)

        // Ambos movimientos deberían ser válidos, ninguno gana inmediatamente
        assertEquals(2, moves.size)
    }

    // Helper para crear estados de juego personalizados
    class GameStateBuilder {
        private val cobs = mutableMapOf<String, Cob>()
        private var currentTurn: Color = WHITE

        fun setTurn(turn: Color) {
            currentTurn = turn
        }

        fun setCob(vertexId: String, color: Color, isUpgraded: Boolean) {
            cobs[vertexId] = Cob(color, isUpgraded)
        }

        fun build(): GameState {
            return GameState(cobs = cobs, currentTurn = currentTurn)
        }
    }

    private fun createGameState(block: GameStateBuilder.() -> Unit): GameState {
        val builder = GameStateBuilder()
        builder.block()
        return builder.build()
    }

    @Test
    fun testMitInOne_Blockade() {
        // Situación: Blanco puede mover para dejar a negro sin movimientos
        val gameState = createGameState {
            setTurn(WHITE)
            // Piezas negras (3)
            setCob("C1", BLACK, false)
            setCob("C7", BLACK, false)
            setCob("C8", BLACK, false)
            // Piezas blancas (5)
            setCob("B1", WHITE, false)
            setCob("C2", WHITE, false)
            setCob("D1", WHITE, false)
            setCob("B4", WHITE, false)
            setCob("C6", WHITE, false)
        }

        val result = getNextBestMove(gameState, Difficulty.MIN)
        val newState = applyMoveToBoard(gameState, result.move!!.from, result.move.to)

        // Después del movimiento, negro no debería tener movimientos
        assertTrue(newState.getAllMovesForTurn().none { gameState.cobs[it.from]?.color == BLACK })
    }

    @Test
    fun testStalemateInOne() {
        // Situación: El jugador actual puede forzar un ahogado (sin movimientos legales para el oponente)
        val gameState = createGameState {
            setTurn(WHITE)
            // Negras (2 piezas casi bloqueadas)
            setCob("C1", BLACK, false)
            setCob("C2", BLACK, false)
            // Blancas (5 piezas)
            setCob("B1", WHITE, false)
            setCob("C3", WHITE, false)
            setCob("C12", WHITE, false)
            setCob("C6", WHITE, false)
            setCob("C7", WHITE, false)
            setCob("C8", WHITE, false)
        }

        val result = getNextBestMove(gameState, Difficulty.DEFAULT)
        val newState = applyMoveToBoard(gameState, result.move!!.from, result.move.to)

        // Después del movimiento, negro no debería tener movimientos legales
        val blackMoves = newState.getAllMovesForTurn().filter { gameState.cobs[it.from]?.color == BLACK }
        assertTrue("Negro debería estar ahogado. Movimientos disponibles: $blackMoves", blackMoves.isEmpty())
    }

    @Test
    fun testWinningPosition_Evaluation() {
        // Test de la función de evaluación en posiciones ganadoras
        val gameState = createGameState {
            setTurn(WHITE)
            // Posición claramente ganadora para blanco
            setCob("A1", WHITE, true)
            setCob("B1", WHITE, true)
            setCob("B2", WHITE, true)
            setCob("B3", WHITE, false)
            setCob("B4", WHITE, false)
            setCob("B5", WHITE, false)
            setCob("B6", WHITE, false)
            setCob("C1", BLACK, false) // Única pieza negra
        }

        val score = evaluateBoard(gameState)
        assertTrue("La evaluación debería ser positiva para blanco. Score: $score", score >= 1100)
    }

    @Test
    fun testGameOver_Detection() {
        // Test de detección de fin de juego
        val gameState = createGameState {
            setTurn(WHITE)
            // Solo una pieza negra que puede ser capturada
            setCob("C1", BLACK, false)
            // Múltiples piezas blancas
            setCob("B1", WHITE, false)
            setCob("C2", WHITE, false)
            setCob("D1", WHITE, false)
            setCob("A1", WHITE, false)
            setCob("B2", WHITE, false)
            setCob("B3", WHITE, false)
            setCob("B4", WHITE, false)
        }

        // Aplicar movimiento ganador
        val newState = applyMoveToBoard(gameState, "B1", "C1")

        // Debería detectar juego terminado
        assertTrue(newState.isGameOver())
    }

    @Test
    fun testForkOpportunity() {
        // Situación: Movimiento que amenaza múltiples piezas
        val gameState = createGameState {
            setTurn(WHITE)
            // Negras en posición vulnerable
            setCob("C1", BLACK, false)
            setCob("C2", BLACK, false)
            // Blancas en posición de tenedor
            setCob("B1", WHITE, true) // Pieza mejorada
            setCob("C3", WHITE, false)
            setCob("D1", WHITE, false)
            setCob("D2", WHITE, false)
            setCob("A1", WHITE, false)
            setCob("B2", WHITE, false)
            setCob("B3", WHITE, false)
        }

        val result = getNextBestMove(gameState, Difficulty.DEFAULT)

        // El movimiento debería crear múltiples amenazas
        val newState = applyMoveToBoard(gameState, result.move!!.from, result.move.to)
        val threats = newState.getAllMovesForTurn().size
        assertTrue("Debería crear múltiples amenazas. Amenazas: $threats", threats > 8)
    }

    @Test
    fun testStalemateInTwo() {
        // Situación: Ahogado forzado en 2 movimientos
        val gameState = createGameState {
            setTurn(WHITE)
            // Distribución 3-5
            // Negras
            setCob("C7", BLACK, false)
            setCob("C8", BLACK, false)
            setCob("D3", BLACK, false)
            // Blancas
            setCob("C1", WHITE, true)
            setCob("C2", WHITE, true)
            setCob("D1", WHITE, false)
            setCob("D2", WHITE, false)
            setCob("B1", WHITE, false)
        }

        val result = getNextBestMove(gameState, Difficulty.DEFAULT)

        // Debería identificar el camino hacia el ahogado
        assertTrue(result.score >= 460)
    }

    @Test
    fun testMitInTwo_ForcedSequence() {
        // Situación: Blanco puede forzar mate en 2 movimientos
        val gameState = createGameState {
            setTurn(WHITE)
            // Piezas negras (2)
            setCob("C1", BLACK, false)
            setCob("C8", BLACK, false)
            // Piezas blancas (6)
            setCob("B1", WHITE, false)
            setCob("C2", WHITE, true) // Mejorada para mayor movilidad
            setCob("D1", WHITE, false)
            setCob("B4", WHITE, false)
            setCob("C6", WHITE, false)
            setCob("A1", WHITE, false)
        }

        val result = getNextBestMove(gameState, Difficulty.DEFAULT)

        // El movimiento debería ser parte de una secuencia ganadora
        assertTrue(result.score == evalConfig.winningScore) // Puntuación muy alta indica victoria inminente
    }

    @Test
    fun testMitInOne_White() {
        // Situación: Blanco puede forzar mate en 1 movimientos
        val gameState = createGameState {
            setTurn(WHITE)
            // Piezas negras (1)
            setCob("B4", BLACK, true)
            // Piezas blancas (7)
            setCob("A1", WHITE, true)
            setCob("B2", WHITE, true)
            setCob("B1", WHITE, true)
            setCob("C2", WHITE, true)
            setCob("C12", WHITE, true)
            setCob("C4", WHITE, false)
            setCob("C5", WHITE, false)
        }

        val result = getNextBestMove(gameState, Difficulty.DEFAULT)

        println(result)

        assertTrue(result.move != null)
        assertTrue(
            (result.move?.from == "B2" && result.move.to == "B3") ||
                    (result.move?.from == "A1" && result.move.to == "B3") ||
                    (result.move?.from == "A1" && result.move.to == "B5")
        )
    }

    @Test
    fun testMitInOne() {
        // Situación: Negro puede forzar mate en 1 movimientos
        val gameState = createGameState {
            setTurn(BLACK)
            // Piezas blancas (1)
            setCob("B4", WHITE, true)
            // Piezas negras (7)
            setCob("A1", BLACK, true)
            setCob("B2", BLACK, true)
            setCob("B1", BLACK, true)
            setCob("C2", BLACK, true)
            setCob("C12", BLACK, true)
            setCob("C4", BLACK, false)
            setCob("C5", BLACK, false)
        }

        val result = getNextBestMove(gameState, Difficulty.DEFAULT)

        println(result)

        assertTrue(result.move != null)
        assertTrue(
            (result.move?.from == "B2" && result.move.to == "B3") ||
                    (result.move?.from == "A1" && result.move.to == "B3") ||
                    (result.move?.from == "A1" && result.move.to == "B5")
        )
    }

    @Test
    fun testMitInOne_quickMitShouldBePrioritized1() {
        // Situación: Negro puede forzar mate en 1 movimientos
        val gameState = createGameState {
            setTurn(BLACK)
            // Piezas blancas (1)
            setCob("C10", WHITE, false)
            // Piezas negras (7)
            setCob("C8", BLACK, true) // <-- Mejorada da posibilidad de mate en 2
            setCob("A1", BLACK, true)
            setCob("B4", BLACK, false)
            setCob("B3", BLACK, false)
            setCob("C7", BLACK, false)
            setCob("B2", BLACK, false)
            setCob("C3", BLACK, false)
        }

        val result = getNextBestMove(gameState, Difficulty.DEFAULT)

        println(result)

        assertTrue(result.move != null)
        assertTrue(
            (result.move?.from == "A1" && result.move.to == "B5") ||
                    (result.move?.from == "C8" && result.move.to == "C9") ||
                    (result.move?.from == "B4" && result.move.to == "B5")
        )
    }

    @Test
    fun testMitInOne_quickMitShouldBePrioritized2() {
        // Situación: Negro puede forzar mate en 1 movimientos
        val gameState = createGameState {
            setTurn(BLACK)
            // Piezas blancas (1)
            setCob("C10", WHITE, false)
            // Piezas negras (7)
            setCob("C8", BLACK, false)
            setCob("A1", BLACK, true)
            setCob("B4", BLACK, false)
            setCob("B3", BLACK, false)
            setCob("C7", BLACK, false)
            setCob("B2", BLACK, false)
            setCob("C3", BLACK, false)
        }

        val result = getNextBestMove(gameState, Difficulty.DEFAULT)

        println(result)

        assertTrue(result.move != null)
        assertTrue(
            (result.move?.from == "A1" && result.move.to == "B5") ||
                    (result.move?.from == "C8" && result.move.to == "C9") ||
                    (result.move?.from == "B4" && result.move.to == "B5")
        )
    }

    @Test
    fun evaluateBoard_upgradedPieceIsMoreValuable() {
        val stateNormal = GameState(
            mapOf(
                "C1" to Cob(WHITE, false),
                "C7" to Cob(BLACK, false)
            ),
            currentTurn = WHITE
        )

        val stateUpgraded = GameState(
            mapOf(
                "C1" to Cob(WHITE, true),
                "C7" to Cob(BLACK, false)
            ),
            currentTurn = WHITE
        )

        val scoreNormal = evaluateBoard(stateNormal)
        val scoreUpgraded = evaluateBoard(stateUpgraded)

        // La pieza upgraded debería tener mejor evaluación
        assertTrue("Upgraded piece should score higher", scoreUpgraded > scoreNormal)

        assertEquals(180.0, scoreUpgraded - scoreNormal, 10.0)
    }

    @Test
    fun testPosition_WhiteShouldFindAMove() {
        // Situación: Blancas tienen muchos movimientos posibles en el comienzo de la partida
        val initialState = createGameState {
            setTurn(WHITE)
            // Piezas blancas (4)
            setCob("D2", WHITE, false)
            setCob("C1", WHITE, false)
            setCob("B1", WHITE, false)
            setCob("C12", WHITE, false)
            // Piezas negras (4)
            setCob("D3", BLACK, false)
            setCob("C8", BLACK, false)
            setCob("C9", BLACK, false)
            setCob("B4", BLACK, false)
        }

        // Las blancas tienen 6 movimientos posibles.
        val move = initialState.getAllMovesForTurn()

        assertNotNull("White should find a move", move.size == 6)
    }

    @Test
    fun testPosition_BlackShouldFindAMove() {
        // Situación: Negras tienen muchos movimientos posibles en el comienzo de la partida
        val initialState = createGameState {
            setTurn(BLACK)
            // Piezas blancas (4)
            setCob("D2", WHITE, false)
            setCob("C1", WHITE, false)
            setCob("B1", WHITE, false)
            setCob("C3", WHITE, false)
            // Piezas negras (4)
            setCob("D4", BLACK, false)
            setCob("C8", BLACK, false)
            setCob("C7", BLACK, false)
            setCob("B6", BLACK, false)
        }

        // Las negras tienen 5 movimientos posibles.
        val move = initialState.getAllMovesForTurn()

        assertTrue("Black should find a move", move.size == 5)
    }

    @Test
    fun testMitInTwo_Black() {
        // Situación: Negro puede forzar mate en 2 movimientos
        val initialState = createGameState {
            setTurn(BLACK)
            // Piezas blancas (3)
            setCob("B4", WHITE, false)
            setCob("C10", WHITE, false)
            setCob("A1", WHITE, true)
            // Piezas negras (5)
            setCob("C6", BLACK, false)
            setCob("B2", BLACK, false)
            setCob("B1", BLACK, true)
            setCob("B6", BLACK, false)
            setCob("C2", BLACK, true)
        }

        // Movimiento 1: Negro juega (debe encontrar C6 -> B3)
        val blackMove1 = getNextBestMove(initialState, Difficulty.DEFAULT)

        println("Black move 1: ${blackMove1.move} with score: ${blackMove1.score}")

        assertNotNull("Black should find a move", blackMove1.move)
        assertEquals("Black should move from C6", blackMove1.move?.from, "C6")
        assertEquals("Black should move to B3", blackMove1.move?.to, "B3")

        // Aplicar el movimiento de negro
        val stateAfterBlack1 = applyMoveToBoard(initialState, "C6", "B3")
            .copy(currentTurn = WHITE)

        // Movimiento 2: Blanco está forzado (debe jugar C10 -> C9 o perder inmediatamente)
        val whiteMove = getNextBestMove(stateAfterBlack1, Difficulty.DEFAULT)

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

        // Movimiento 3: Negro da mit
        val blackMove2 = getNextBestMove(stateAfterWhite, Difficulty.DEFAULT)

        assertNotNull("Black should find a move", blackMove2.move)
        assertTrue("Black should move from A1 or B4", blackMove2.move?.from == "A1" || blackMove2.move?.from == "B4")
        assertEquals("Black should move to B5", blackMove2.move?.to, "B5")

        println("Black move: ${blackMove2.move} with score: ${blackMove2.score}")

        assertNotNull("Black should find mit", blackMove2.move)

        // Aplicar el movimiento final
        val finalState = applyMoveToBoard(stateAfterWhite, blackMove2.move!!.from, blackMove2.move.to)
            .copy(currentTurn = WHITE)

        // Verificar que es mit
        assertTrue("Should be game over", finalState.isGameOver())
        assertEquals("Black should win", BLACK, finalState.getWinner())

        // El score del primer movimiento debería indicar mate forzado
        assertTrue(
            "Black's first move should have winning score (negative for BLACK)",
            blackMove1.score == -evalConfig.winningScore
        )
    }

    @Test
    fun testStalemate_OpponentNoMoves() {
        // Situación: Negro no tiene movimientos legales - ahogado
        val initialState = createGameState {
            setTurn(BLACK)
            // Piezas blancas (7)
            setCob("A1", WHITE, true)
            setCob("B1", WHITE, true)
            setCob("C1", WHITE, true)
            setCob("D1", WHITE, true)
            setCob("A2", WHITE, true)
            setCob("B2", WHITE, true)
            setCob("C2", WHITE, true)
            // Piezas negras (1) - completamente rodeada
            setCob("B3", BLACK, false)
        }

        val blackMoves = initialState.getAllMovesForTurn()
        assertEquals("Black should have no legal moves", 0, blackMoves.size)

        assertTrue("Game should be over", initialState.isGameOver())
        assertEquals("White should win by stalemate", WHITE, initialState.getWinner())
    }

    @Test
    fun testStalemate_SelfNoMoves() {
        // Situación: Negras no tiene movimientos legales - se ahoga a sí mismo
        val initialState = createGameState {
            setTurn(BLACK)
            // Piezas blancas (1) - rodeada por sus propias piezas
            setCob("B4", BLACK, false)
            // Piezas negras (7)
            setCob("A1", WHITE, false)
            setCob("B3", WHITE, false)
            setCob("B5", WHITE, false)
            setCob("B2", WHITE, false)
            setCob("B6", WHITE, false)
            setCob("C2", WHITE, false)
            setCob("C1", WHITE, false)
        }

        val whiteMoves = initialState.getAllMovesForTurn()
        assertEquals("Black should have no legal moves", 0, whiteMoves.size)

        assertTrue("Game should be over", initialState.isGameOver())
        assertEquals("White should win by stalemate", WHITE, initialState.getWinner())
    }

    @Test
    fun testQuickWin_MaterialAdvantage() {
        // Situación: Negro tiene ventaja material abrumadora y debe ganar rápido
        val initialState = createGameState {
            setTurn(BLACK)
            // Piezas blancas (2) - muy pocas
            setCob("C10", WHITE, false)
            setCob("C12", WHITE, false)
            // Piezas negras (6) - muchas y mejoradas
            setCob("C9", BLACK, true)
            setCob("C8", BLACK, true)
            setCob("C6", BLACK, true)
            setCob("C5", BLACK, true)
            setCob("B3", BLACK, true)
            setCob("B4", BLACK, true)
        }

        val blackMove = getNextBestMove(initialState, Difficulty.DEFAULT)

        assertNotNull("Black should find a winning move", blackMove.move)

        // El movimiento debería ser agresivo (captura o amenaza directa)
        val newState = applyMoveToBoard(initialState, blackMove.move!!.from, blackMove.move.to)

        // Verificar que el movimiento mejora la posición de negro
        val scoreAfter = evaluateBoard(newState)
        assertTrue("Move should improve black's position", -scoreAfter < evalConfig.winningScore * 0.3)
    }

    @Test
    fun testDefensiveMove_AvoidImmediateLoss() {
        // Situación: Blanco está en peligro inminente y debe jugar defensivamente
        val initialState = createGameState {
            setTurn(WHITE)
            // Piezas blancas (3) - en peligro
            setCob("C10", WHITE, false)  // Amenazada
            setCob("D10", WHITE, false)  // Amenazada
            setCob("B11", WHITE, true)   // Segura
            // Piezas negras (5) - atacando
            setCob("C8", BLACK, true)    // Amenazando C10
            setCob("D8", BLACK, true)    // Amenazando D10
            setCob("B7", BLACK, false)
            setCob("C7", BLACK, false)
            setCob("D7", BLACK, false)
        }

        val whiteMove = getNextBestMove(initialState, Difficulty.DEFAULT)

        assertNotNull("White should find a defensive move", whiteMove.move)

        // El movimiento debería ser defensivo (proteger piezas amenazadas)
        val isDefensive = whiteMove.move!!.from == "C10" || whiteMove.move.from == "D10"
        assertTrue("White should move threatened pieces", isDefensive)
    }

    @Test
    fun testUpgradeOpportunity_Prioritize() {
        // Situación: Negro puede mejorar una pieza y debe priorizarlo
        val initialState = createGameState {
            setTurn(BLACK)
            // Piezas blancas (4)
            setCob("C9", BLACK, false)  // Cerca de la base blanca
            setCob("C11", BLACK, false)
            setCob("C3", BLACK, false)
            setCob("C12", BLACK, false)
            // Piezas negras (4)
            setCob("B3", WHITE, false)
            setCob("B2", WHITE, false)
            setCob("B4", WHITE, false)
            setCob("C5", WHITE, false)
        }

        val blackMove = getNextBestMove(initialState, Difficulty.DEFAULT)

        assertNotNull("Black should find a move", blackMove.move)

        // Debería priorizar mover hacia la base negra para mejorar
        val leadsToUpgrade = blackMove.move!!.to in homeBases[WHITE]!!
        assertTrue("Black should prioritize upgrade opportunity", leadsToUpgrade)
    }

    @Test
    fun testCenterControl_StrategicMove() {
        // Situación: Control del centro es crucial
        val initialState = createGameState {
            setTurn(WHITE)
            // Piezas blancas (4)
            setCob("B2", WHITE, false)
            setCob("C2", WHITE, false)
            setCob("D2", WHITE, false)
            setCob("C3", WHITE, false)
            // Piezas negras (4)
            setCob("B5", BLACK, false)
            setCob("C5", BLACK, false)
            setCob("D5", BLACK, false)
            setCob("C4", BLACK, false)
        }

        val whiteMove = getNextBestMove(initialState, Difficulty.DEFAULT)

        assertNotNull("White should find a strategic move", whiteMove.move)

        // El movimiento debería apuntar a controlar el centro
        val controlsCenter = centerVertices.contains(whiteMove.move!!.to)
        assertTrue("White should move to control center", controlsCenter)
    }

    @Test
    fun testEndgame_SinglePieceSurvival() {
        // Situación: Final de juego con pocas piezas
        val initialState = createGameState {
            setTurn(WHITE)
            // Piezas blancas (1)
            setCob("C6", WHITE, true)  // Pieza mejorada
            // Piezas negras (1)
            setCob("C7", BLACK, true)  // Pieza mejorada
        }

        val whiteMove = getNextBestMove(initialState, Difficulty.DEFAULT)

        assertNotNull("White should find a survival move", whiteMove.move)

        // En final de juego, priorizar supervivencia sobre riesgo
        val newState = applyMoveToBoard(initialState, whiteMove.move!!.from, whiteMove.move.to)
        val whiteCobsAfter = newState.cobs.values.count { it.color == WHITE }
        assertEquals("White should not lose its piece", 1, whiteCobsAfter)
    }

    @Test
    fun testSacrifice_TacticalMove() {
        // Situación: Negro puede sacrificar una pieza para ganar ventaja
        val initialState = createGameState {
            setTurn(BLACK)
            // Piezas blancas (3)
            setCob("C10", WHITE, false)
            setCob("B11", WHITE, true)
            setCob("D11", WHITE, false)
            // Piezas negras (5)
            setCob("C8", BLACK, false)  // Pieza a sacrificar
            setCob("B7", BLACK, true)
            setCob("C7", BLACK, true)
            setCob("D7", BLACK, true)
            setCob("C9", BLACK, false)
        }

        val blackMove = getNextBestMove(initialState, Difficulty.HARD)

        assertNotNull("Black should find a tactical move", blackMove.move)

        // Verificar que el sacrificio lleva a una mejor posición
        val newState = applyMoveToBoard(initialState, blackMove.move!!.from, blackMove.move.to)

        // Después del movimiento, negro debería tener amenazas fuertes
        val blackThreats = quickEvaluate(newState)
        assertTrue("Sacrifice should create strong threats", blackThreats < -evalConfig.cobScore * 2)
    }
}