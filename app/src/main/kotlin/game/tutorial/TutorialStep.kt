package com.agustin.tarati.game.tutorial

import androidx.compose.ui.graphics.Color
import com.agustin.tarati.R
import com.agustin.tarati.game.core.Cob
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameBoard.bridgeEdges
import com.agustin.tarati.game.core.GameBoard.bridgeVertices
import com.agustin.tarati.game.core.GameBoard.circumferenceEdges
import com.agustin.tarati.game.core.GameBoard.circumferenceVertices
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.core.createGameState
import com.agustin.tarati.ui.components.board.animation.EdgeHighlight
import com.agustin.tarati.ui.components.board.animation.HighlightAnimation
import com.agustin.tarati.ui.components.board.animation.VertexHighlight

abstract class TutorialStep(
    val titleResId: Int,
    val descriptionResId: Int,
    val animations: List<HighlightAnimation> = emptyList(),
    val gameState: GameState,
    val autoAdvanceDelay: Long? = null,
    val interactionRequired: Boolean = false,
    val onStepStart: (() -> Unit)? = null
)

abstract class InteractiveTutorialStep(
    titleResId: Int,
    descriptionResId: Int,
    animations: List<HighlightAnimation> = emptyList(),
    gameState: GameState,
    val expectedMoves: List<Move> = listOf(),
    val validateMove: ((Move) -> Boolean) = { true },
    onStepStart: (() -> Unit)? = null
) : TutorialStep(
    titleResId = titleResId,
    descriptionResId = descriptionResId,
    animations = animations,
    gameState = gameState,
    interactionRequired = true,
    onStepStart = onStepStart
) {
    fun isExpectedMove(move: Move): Boolean {
        return if (expectedMoves.isNotEmpty()) {
            expectedMoves.contains(move)
        } else {
            validateMove(move)
        }
    }
}

class IntroductionStep : TutorialStep(
    titleResId = R.string.tutorial_introduction_title,
    descriptionResId = R.string.tutorial_introduction_description,
    animations = emptyList(),
    gameState = createGameState {
        clearCobs()
    },
    autoAdvanceDelay = 4000L
)

class CompletedStep : TutorialStep(
    titleResId = R.string.tutorial_completed_title,
    descriptionResId = R.string.tutorial_completed_description,
    animations = emptyList(),
    gameState = createGameState { },
    autoAdvanceDelay = 4000L
)

class CenterStep : TutorialStep(
    titleResId = R.string.tutorial_center_title,
    descriptionResId = R.string.tutorial_center_description,
    animations = createCenterAnimations(),
    gameState = createGameState {
        clearCobs()
    },
    autoAdvanceDelay = 8000L
)

class BridgeStep : TutorialStep(
    titleResId = R.string.tutorial_bridge_title,
    descriptionResId = R.string.tutorial_bridge_description,
    animations = createBridgeAnimations(),
    gameState = createGameState {
        clearCobs()
    },
    autoAdvanceDelay = 10000L
)

class CircumferenceStep : TutorialStep(
    titleResId = R.string.tutorial_circumference_title,
    descriptionResId = R.string.tutorial_circumference_description,
    animations = createCircumferenceAnimations(),
    gameState = createGameState {
        clearCobs()
    },
    autoAdvanceDelay = 12000L
)

class DomesticBasesStep : TutorialStep(
    titleResId = R.string.tutorial_domestic_bases_title,
    descriptionResId = R.string.tutorial_domestic_bases_description,
    animations = createDomesticAnimations(),
    gameState = createGameState {
        clearCobs()
    },
    autoAdvanceDelay = 6000L
)

class CobsStep : TutorialStep(
    titleResId = R.string.tutorial_cobs_title,
    descriptionResId = R.string.tutorial_cobs_description,
    animations = createCobsAnimations(),
    gameState = createGameState {
        clearCobs()
        setCob("C1", Cob(WHITE, false))
        setCob("C2", Cob(WHITE, false))
        setCob("D1", Cob(WHITE, false))
        setCob("D2", Cob(WHITE, false))
    },
    autoAdvanceDelay = 6000L
)

class BasicMovesStep : InteractiveTutorialStep(
    titleResId = R.string.tutorial_basic_moves_title,
    descriptionResId = R.string.tutorial_basic_moves_description,
    animations = createMoveAnimations(),
    gameState = createGameState {
        clearCobs()
        setCob("C1", Cob(WHITE, false))
        setCob("C2", Cob(WHITE, false))
        setCob("D1", Cob(WHITE, false))
        setCob("D2", Cob(WHITE, false))
    },
    expectedMoves = listOf(Move("C1", "B1"), Move("C2", "B1"), Move("C1", "C12"), Move("C2", "C3")),
    onStepStart = {
        // TODO: Preparar estado para la interacción
    }
)

class CapturesStep : InteractiveTutorialStep(
    titleResId = R.string.tutorial_captures_title,
    descriptionResId = R.string.tutorial_captures_description,
    animations = createCaptureAnimations(),
    gameState = createGameState {
        clearCobs()
        setCob("C1", Cob(WHITE, false))
        setCob("A1", Cob(BLACK, false))
    },
    expectedMoves = listOf(Move("C1", "B1")),
    onStepStart = {
        // TODO: Preparar estado estado para captura
    }
)

class UpgradeStep : InteractiveTutorialStep(
    titleResId = R.string.tutorial_upgrade_title,
    descriptionResId = R.string.tutorial_upgrade_description,
    animations = createUpgradeAnimations(),
    gameState = createGameState {
        clearCobs()
        setCob("B4", Cob(WHITE, false))
    },
    expectedMoves = listOf(Move("B4", "C7"), Move("B4", "C8")),
    onStepStart = {
        // TODO: Preparar estado para la mejora
    }
)

class CastlingStep : InteractiveTutorialStep(
    titleResId = R.string.tutorial_castling_title,
    descriptionResId = R.string.tutorial_castling_description,
    animations = createCastlingAnimations(),
    gameState = createGameState {
        clearCobs()
        setCob("C1", Cob(WHITE, false))
        setCob("B1", Cob(BLACK, false))
    },
    expectedMoves = listOf(Move("C1", "C2"))
)

private fun createCenterAnimations(): List<HighlightAnimation> {
    return listOf(
        HighlightAnimation.Vertex(VertexHighlight("A1", Color.Green, true, 2000L)),
        HighlightAnimation.Pause(),
        HighlightAnimation.Edge(EdgeHighlight("A1", "B1", Color.Yellow, true, 1500L)),
        HighlightAnimation.Edge(EdgeHighlight("A1", "B2", Color.Yellow, true, 1500L)),
        HighlightAnimation.Edge(EdgeHighlight("A1", "B3", Color.Yellow, true, 1500L)),
        HighlightAnimation.Edge(EdgeHighlight("A1", "B4", Color.Yellow, true, 1500L)),
        HighlightAnimation.Edge(EdgeHighlight("A1", "B5", Color.Yellow, true, 1500L)),
        HighlightAnimation.Edge(EdgeHighlight("A1", "B6", Color.Yellow, true, 1500L))
    )
}

private fun createBridgeAnimations(): List<HighlightAnimation> {
    val animations = mutableListOf<HighlightAnimation>()

    bridgeVertices.forEach { vertex ->
        animations.add(
            HighlightAnimation.Vertex(
                VertexHighlight(vertex, Color(0xFFFFEB3B), true, 1000L)
            )
        )
        animations.add(HighlightAnimation.Pause())
    }

    bridgeEdges.forEach { (from, to) ->
        animations.add(
            HighlightAnimation.Edge(
                EdgeHighlight(from, to, Color(0xFF4CAF50), true, 1200L)
            )
        )
        animations.add(HighlightAnimation.Pause())
    }

    return animations
}

private fun createCircumferenceAnimations(): List<HighlightAnimation> {
    val animations = mutableListOf<HighlightAnimation>()

    circumferenceVertices.forEach { vertex ->
        animations.add(
            HighlightAnimation.Vertex(
                VertexHighlight(vertex, Color(0xFFFF9800), true, 600L)
            )
        )
        animations.add(HighlightAnimation.Pause())
    }

    circumferenceEdges.forEach { (from, to) ->
        animations.add(
            HighlightAnimation.Edge(
                EdgeHighlight(from, to, Color(0xFF4CAFAF), true, 1200L)
            )
        )
        animations.add(HighlightAnimation.Pause())
    }

    return animations
}

private fun createDomesticAnimations(): List<HighlightAnimation> {
    return listOf(
        // Base blanca
        HighlightAnimation.Vertex(VertexHighlight("D1", Color(0xFF99D9BF), true, 1500L)),
        HighlightAnimation.Vertex(VertexHighlight("D2", Color(0xFF99D9BF), true, 1500L)),
        HighlightAnimation.Vertex(VertexHighlight("C1", Color(0xFF99D9BF), true, 1500L)),
        HighlightAnimation.Vertex(VertexHighlight("C2", Color(0xFF99D9BF), true, 1500L)),
        HighlightAnimation.Pause(),
        // Conexiones base blanca
        HighlightAnimation.Edge(EdgeHighlight("D1", "D2", Color(0xFF80BDE7), true, 1200L)),
        HighlightAnimation.Edge(EdgeHighlight("D2", "C2", Color(0xFF80BDE7), true, 1200L)),
        HighlightAnimation.Edge(EdgeHighlight("C2", "C1", Color(0xFF80BDE7), true, 1200L)),
        HighlightAnimation.Edge(EdgeHighlight("C1", "D1", Color(0xFF80BDE7), true, 1200L)),
        HighlightAnimation.Pause(),
        // Base negra
        HighlightAnimation.Vertex(VertexHighlight("D3", Color(0xFFFFEBEE), true, 1500L)),
        HighlightAnimation.Vertex(VertexHighlight("D4", Color(0xFFFFEBEE), true, 1500L)),
        HighlightAnimation.Vertex(VertexHighlight("C7", Color(0xFFFFCDD2), true, 1500L)),
        HighlightAnimation.Vertex(VertexHighlight("C8", Color(0xFFFFCDD2), true, 1500L)),
        HighlightAnimation.Pause(),
        // Conexiones base blanca
        HighlightAnimation.Edge(EdgeHighlight("D3", "D4", Color(0xFF80BDE7), true, 1200L)),
        HighlightAnimation.Edge(EdgeHighlight("D4", "C8", Color(0xFF80BDE7), true, 1200L)),
        HighlightAnimation.Edge(EdgeHighlight("C8", "C7", Color(0xFF80BDE7), true, 1200L)),
        HighlightAnimation.Edge(EdgeHighlight("C7", "D3", Color(0xFF80BDE7), true, 1200L)),
        HighlightAnimation.Pause(),
    )
}

private fun createCobsAnimations(): List<HighlightAnimation> {
    return listOf(
        HighlightAnimation.Vertex(VertexHighlight("D1", Color.Yellow, true, 1000L)),
        HighlightAnimation.Vertex(VertexHighlight("D2", Color.Yellow, true, 1000L)),
        HighlightAnimation.Vertex(VertexHighlight("D3", Color.Yellow, true, 1000L)),
        HighlightAnimation.Vertex(VertexHighlight("D4", Color.Yellow, true, 1000L)),
        HighlightAnimation.Pause(),
        HighlightAnimation.Vertex(VertexHighlight("C1", Color(0xFF4CAF50), true, 1000L)),
        HighlightAnimation.Vertex(VertexHighlight("C2", Color(0xFF4CAF50), true, 1000L)),
        HighlightAnimation.Vertex(VertexHighlight("C7", Color(0xFF4CAF50), true, 1000L)),
        HighlightAnimation.Vertex(VertexHighlight("C8", Color(0xFF4CAF50), true, 1000L))
    )
}

private fun createMoveAnimations(): List<HighlightAnimation> {
    return listOf(
        HighlightAnimation.Vertex(VertexHighlight("C1", Color.Yellow, true, 2000L)),
        HighlightAnimation.Pause(),
        HighlightAnimation.Edge(EdgeHighlight("C1", "B1", Color(0xFF4CAF50), true, 1500L)),
        HighlightAnimation.Vertex(VertexHighlight("B1", Color(0xFF98DA9B), true, 1500L)),
        HighlightAnimation.Edge(EdgeHighlight("C1", "C12", Color(0xFF4CAF50), true, 1500L)),
        HighlightAnimation.Vertex(VertexHighlight("C12", Color.Yellow, true, 2000L)),
        HighlightAnimation.Pause(),
        HighlightAnimation.Edge(EdgeHighlight("C2", "B1", Color(0xFF4CA7AF), true, 1500L)),
        HighlightAnimation.Vertex(VertexHighlight("B1", Color(0xFF98DA9B), true, 1500L)),
        HighlightAnimation.Edge(EdgeHighlight("C2", "C3", Color(0xFF4CA7AF), true, 1500L)),
        HighlightAnimation.Vertex(VertexHighlight("C3", Color(0xFF98DA9B), true, 1500L)),
        HighlightAnimation.Pause(),
        HighlightAnimation.Vertex(VertexHighlight("B1", Color(0xFF9DAF4C), true, 2000L)),
        HighlightAnimation.Vertex(VertexHighlight("C12", Color(0xFF4CAF50), true, 2000L)),
        HighlightAnimation.Vertex(VertexHighlight("C3", Color(0xFF4CAFA3), true, 2000L)),
    )
}

private fun createCaptureAnimations(): List<HighlightAnimation> {
    return listOf(
        HighlightAnimation.Vertex(VertexHighlight("C1", Color.Yellow, true, 1500L)),
        HighlightAnimation.Pause(),
        HighlightAnimation.Edge(EdgeHighlight("C1", "B1", Color(0xFFF44336), true, 1500L)),
        HighlightAnimation.Vertex(VertexHighlight("B1", Color(0xFF4CAF50), true, 2000L)),
        HighlightAnimation.Pause(),
        HighlightAnimation.Vertex(VertexHighlight("A1", Color(0xFFF44336), true, 2000L)),
    )
}

private fun createUpgradeAnimations(): List<HighlightAnimation> {
    return listOf(
        HighlightAnimation.Vertex(VertexHighlight("B4", Color.Yellow, true, 1500L)),
        HighlightAnimation.Pause(),
        HighlightAnimation.Edge(EdgeHighlight("B4", "C7", Color(0xFF2196F3), true, 1500L)),
        HighlightAnimation.Vertex(VertexHighlight("C7", Color(0xFF2196F3), true, 2000L)),
        HighlightAnimation.Pause(),
        HighlightAnimation.Vertex(VertexHighlight("C7", Color(0xFF9C27B0), true, 2000L))
    )
}

private fun createCastlingAnimations(): List<HighlightAnimation> {
    return listOf(
        HighlightAnimation.Vertex(VertexHighlight("C1", Color.Yellow, true, 1500L)),
        HighlightAnimation.Vertex(VertexHighlight("C2", Color.Yellow, true, 1500L)),
        HighlightAnimation.Pause(),
        HighlightAnimation.Vertex(VertexHighlight("B1", Color(0xFFF44336), true, 1500L)),
        HighlightAnimation.Pause(),
        HighlightAnimation.Edge(EdgeHighlight("C1", "C2", Color(0xFF9C27B0), true, 2000L)),
        HighlightAnimation.Pause(),
        HighlightAnimation.Vertex(VertexHighlight("B1", Color(0xFF4CAF50), true, 2000L))
    )
}