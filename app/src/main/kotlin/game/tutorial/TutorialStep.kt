// TutorialStep.kt
package com.agustin.tarati.game.tutorial

import androidx.compose.ui.unit.dp
import com.agustin.tarati.game.core.Cob
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.ui.components.tutorial.BubbleConfig
import com.agustin.tarati.ui.components.tutorial.BubblePosition
import androidx.compose.ui.graphics.Color as ComposeColor

// Definición única de Highlight
data class Highlight(
    val vertexId: String,
    val color: ComposeColor,
    val pulse: Boolean = false,
    val message: String? = null
)

// Interface base para todos los pasos del tutorial
interface TutorialStep {
    val id: String
    val title: String
    val description: String
    val gameState: GameState
    val highlights: List<Highlight>
    val requiredMove: Move?
    val autoAdvance: Boolean
    val durationMs: Long
    val bubbleConfig: BubbleConfig
}

// Pasos específicos del tutorial como objetos
object IntroductionStep : TutorialStep {
    override val id = "introduction"
    override val title = "Bienvenido a Tarati"
    override val description =
        "Tarati es un juego de estrategia creado por George Spencer Brown. Aprende los movimientos básicos."
    override val gameState = GameState(emptyMap(), Color.WHITE)
    override val highlights = emptyList<Highlight>()
    override val requiredMove = null
    override val autoAdvance = true
    override val durationMs = 3000L
    override val bubbleConfig = BubbleConfig(BubblePosition.BOTTOM_CENTER)
}

object BoardLayoutStep : TutorialStep {
    override val id = "board_layout"
    override val title = "El Tablero"
    override val description = "El tablero tiene vértices conectados. Las piezas se mueven entre vértices adyacentes."
    override val gameState = GameState(emptyMap(), Color.WHITE)
    override val highlights = listOf(
        Highlight("A1", ComposeColor.Yellow.copy(alpha = 0.3f), pulse = true, message = "Centro"),
        Highlight("C1", ComposeColor.Blue.copy(alpha = 0.3f), message = "Base Blanca"),
        Highlight("C7", ComposeColor.Red.copy(alpha = 0.3f), message = "Base Negra")
    )
    override val requiredMove = null
    override val autoAdvance = true
    override val durationMs = 4000L
    override val bubbleConfig = BubbleConfig(
        position = BubblePosition.TOP_CENTER,
        width = 300.dp
    )
}

object BasicMoveStep : TutorialStep {
    override val id = "basic_move"
    override val title = "Movimientos Básicos"
    override val description =
        "Las piezas Cob se mueven hacia adelante o a vértices adyacentes. Toca una pieza para ver los movimientos válidos."
    override val gameState: GameState = createBasicMoveState()
    override val highlights = listOf(
        Highlight("C2", ComposeColor.Green.copy(alpha = 0.5f), pulse = true)
    )
    override val requiredMove = Move("C2", "B1")
    override val autoAdvance = false
    override val durationMs = 0L
    override val bubbleConfig = BubbleConfig(BubblePosition.BOTTOM_LEFT)

    private fun createBasicMoveState(): GameState {
        return GameState(
            cobs = mapOf(
                "C2" to Cob(Color.WHITE),
                "C7" to Cob(Color.BLACK)
            ),
            currentTurn = Color.WHITE
        )
    }
}

object CaptureStep : TutorialStep {
    override val id = "capture"
    override val title = "Captura de Piezas"
    override val description = "Al moverte junto a una pieza enemiga, la capturas y cambia a tu color."
    override val gameState: GameState = createCaptureState()
    override val highlights = listOf(
        Highlight("B1", ComposeColor.Green.copy(alpha = 0.5f), pulse = true),
        Highlight("B6", ComposeColor.Red.copy(alpha = 0.5f), pulse = true)
    )
    override val requiredMove = Move("B1", "B2")
    override val autoAdvance = true
    override val durationMs = 4000L
    override val bubbleConfig = BubbleConfig(BubblePosition.BOTTOM_CENTER)

    private fun createCaptureState(): GameState {
        return GameState(
            cobs = mapOf(
                "B1" to Cob(Color.WHITE),
                "B6" to Cob(Color.BLACK),
                "C1" to Cob(Color.BLACK)
            ),
            currentTurn = Color.WHITE
        )
    }
}

object UpgradeStep : TutorialStep {
    override val id = "upgrade"
    override val title = "Mejora de Piezas"
    override val description =
        "Al llegar a la base enemiga, tu pieza Cob se convierte en Roc, que puede moverse en cualquier dirección."
    override val gameState: GameState = createUpgradeState()
    override val highlights = listOf(
        Highlight("C7", ComposeColor.Green.copy(alpha = 0.5f), pulse = true)
    )
    override val requiredMove = Move("C7", "D3")
    override val autoAdvance = true
    override val durationMs = 4000L
    override val bubbleConfig = BubbleConfig(BubblePosition.BOTTOM_CENTER)

    private fun createUpgradeState(): GameState {
        return GameState(
            cobs = mapOf(
                "C7" to Cob(Color.WHITE),
                "D4" to Cob(Color.BLACK)
            ),
            currentTurn = Color.WHITE
        )
    }
}

object CastlingStep : TutorialStep {
    override val id = "castling"
    override val title = "Movimiento de Enroque"
    override val description =
        "En las bases, puedes realizar un movimiento especial de enroque que captura piezas enemigas en posiciones clave."
    override val gameState: GameState = createCastlingState()
    override val highlights = listOf(
        Highlight("C1", ComposeColor.Green.copy(alpha = 0.5f), pulse = true),
        Highlight("B1", ComposeColor.Red.copy(alpha = 0.5f), pulse = true)
    )
    override val requiredMove = Move("C1", "C2")
    override val autoAdvance = true
    override val durationMs = 4000L
    override val bubbleConfig = BubbleConfig(BubblePosition.BOTTOM_CENTER)

    private fun createCastlingState(): GameState {
        return GameState(
            cobs = mapOf(
                "C1" to Cob(Color.WHITE),
                "B1" to Cob(Color.BLACK)
            ),
            currentTurn = Color.WHITE
        )
    }
}