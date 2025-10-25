package com.agustin.tarati.game.tutorial

import com.agustin.tarati.R
import com.agustin.tarati.game.core.Cob
import com.agustin.tarati.game.core.CobColor.BLACK
import com.agustin.tarati.game.core.CobColor.WHITE
import com.agustin.tarati.game.core.GameBoard.absoluteCenterToBridgeEdges
import com.agustin.tarati.game.core.GameBoard.bridgeEdges
import com.agustin.tarati.game.core.GameBoard.bridgeVertices
import com.agustin.tarati.game.core.GameBoard.circumferenceEdges
import com.agustin.tarati.game.core.GameBoard.circumferenceVertices
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.core.createGameState
import com.agustin.tarati.game.core.initialGameState
import com.agustin.tarati.ui.components.board.animation.EdgeHighlight
import com.agustin.tarati.ui.components.board.animation.HighlightAnimation
import com.agustin.tarati.ui.components.board.animation.VertexHighlight

abstract class TutorialStep(
    val titleResId: Int,
    val descriptionResId: Int,
    val animations: List<HighlightAnimation> = emptyList(),
    val gameState: GameState,
    val autoAdvanceDelay: Long? = null,
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
    gameState = createGameState { initialGameState() },
    autoAdvanceDelay = 4000L
)

class CenterStep : TutorialStep(
    titleResId = R.string.tutorial_center_title,
    descriptionResId = R.string.tutorial_center_description,
    animations = createCenterAnimations(),
    gameState = createGameState {
        clearCobs()
    },
    autoAdvanceDelay = 6000L
)

class BridgeStep : TutorialStep(
    titleResId = R.string.tutorial_bridge_title,
    descriptionResId = R.string.tutorial_bridge_description,
    animations = createBridgeAnimations(),
    gameState = createGameState {
        clearCobs()
    },
    autoAdvanceDelay = 6000L
)

class CircumferenceStep : TutorialStep(
    titleResId = R.string.tutorial_circumference_title,
    descriptionResId = R.string.tutorial_circumference_description,
    animations = createCircumferenceAnimations(),
    gameState = createGameState {
        clearCobs()
    },
    autoAdvanceDelay = 6000L
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
    val animations = mutableListOf<HighlightAnimation>()
    // Centro Absoluto A
    animations.add(
        HighlightAnimation.Vertex(VertexHighlight("A1", true, startDelay = 500L, persistent = true))
    )
    animations.add(HighlightAnimation.Pause())
    // Fase 1: Iluminar aristas centrales en secuencia
    absoluteCenterToBridgeEdges.forEachIndexed { index, (from, to) ->
        animations.add(
            HighlightAnimation.Edge(
                EdgeHighlight(from, to, true, startDelay = (index * 400).toLong())
            )
        )
        animations.add(HighlightAnimation.Pause())
    }
    animations.add(HighlightAnimation.Pause())
    // Fase 2: Iluminar vértices del puente en secuencia
    bridgeVertices.forEachIndexed { index, vertex ->
        animations.add(
            HighlightAnimation.Vertex(
                VertexHighlight(vertex, true, startDelay = (index * 400).toLong())
            )
        )
    }
    return animations
}

private fun createBridgeAnimations(): List<HighlightAnimation> {
    val animations = mutableListOf<HighlightAnimation>()

    bridgeVertices.forEach { vertex ->
        animations.add(
            HighlightAnimation.Vertex(
                VertexHighlight(vertex, true, startDelay = 1000L)
            )
        )
        animations.add(HighlightAnimation.Pause())
    }

    bridgeEdges.forEach { (from, to) ->
        animations.add(
            HighlightAnimation.Edge(
                EdgeHighlight(from, to, true, startDelay = 1200L)
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
                VertexHighlight(vertex, true, startDelay = 600L)
            )
        )
        animations.add(HighlightAnimation.Pause())
    }

    circumferenceEdges.forEach { (from, to) ->
        animations.add(
            HighlightAnimation.Edge(
                EdgeHighlight(from, to, true, startDelay = 1200L)
            )
        )
        animations.add(HighlightAnimation.Pause())
    }

    return animations
}

private fun createDomesticAnimations(): List<HighlightAnimation> {
    return listOf(
        // Base blanca
        HighlightAnimation.Vertex(VertexHighlight("D1", true, startDelay = 1500L)),
        HighlightAnimation.Vertex(VertexHighlight("D2", true, startDelay = 1500L)),
        HighlightAnimation.Vertex(VertexHighlight("C1", true, startDelay = 1500L)),
        HighlightAnimation.Vertex(VertexHighlight("C2", true, startDelay = 1500L)),
        HighlightAnimation.Pause(),
        // Conexiones base blanca
        HighlightAnimation.Edge(EdgeHighlight("D1", "D2", true, startDelay = 1200L)),
        HighlightAnimation.Edge(EdgeHighlight("D2", "C2", true, startDelay = 1200L)),
        HighlightAnimation.Edge(EdgeHighlight("C2", "C1", true, startDelay = 1200L)),
        HighlightAnimation.Edge(EdgeHighlight("C1", "D1", true, startDelay = 1200L)),
        HighlightAnimation.Pause(),
        // Base negra
        HighlightAnimation.Vertex(VertexHighlight("D3", true, startDelay = 1500L)),
        HighlightAnimation.Vertex(VertexHighlight("D4", true, startDelay = 1500L)),
        HighlightAnimation.Vertex(VertexHighlight("C7", true, startDelay = 1500L)),
        HighlightAnimation.Vertex(VertexHighlight("C8", true, startDelay = 1500L)),
        HighlightAnimation.Pause(),
        // Conexiones base blanca
        HighlightAnimation.Edge(EdgeHighlight("D3", "D4", true, startDelay = 1200L)),
        HighlightAnimation.Edge(EdgeHighlight("D4", "C8", true, startDelay = 1200L)),
        HighlightAnimation.Edge(EdgeHighlight("C8", "C7", true, startDelay = 1200L)),
        HighlightAnimation.Edge(EdgeHighlight("C7", "D3", true, startDelay = 1200L)),
        HighlightAnimation.Pause(),
    )
}

private fun createCobsAnimations(): List<HighlightAnimation> {
    return listOf(
        HighlightAnimation.Vertex(VertexHighlight("D1", true, startDelay = 1000L)),
        HighlightAnimation.Vertex(VertexHighlight("D2", true, startDelay = 1000L)),
        HighlightAnimation.Vertex(VertexHighlight("D3", true, startDelay = 1000L)),
        HighlightAnimation.Vertex(VertexHighlight("D4", true, startDelay = 1000L)),
        HighlightAnimation.Pause(),
        HighlightAnimation.Vertex(VertexHighlight("C1", true, startDelay = 1000L)),
        HighlightAnimation.Vertex(VertexHighlight("C2", true, startDelay = 1000L)),
        HighlightAnimation.Vertex(VertexHighlight("C7", true, startDelay = 1000L)),
        HighlightAnimation.Vertex(VertexHighlight("C8", true, startDelay = 1000L))
    )
}

private fun createMoveAnimations(): List<HighlightAnimation> {
    return listOf(
        HighlightAnimation.Vertex(VertexHighlight("C1", true, startDelay = 2000L)),
        HighlightAnimation.Pause(),
        HighlightAnimation.Edge(EdgeHighlight("C1", "B1", true, startDelay = 1500L)),
        HighlightAnimation.Vertex(VertexHighlight("B1", true, startDelay = 1500L)),
        HighlightAnimation.Edge(EdgeHighlight("C1", "C12", true, startDelay = 1500L)),
        HighlightAnimation.Vertex(VertexHighlight("C12", true, startDelay = 2000L)),
        HighlightAnimation.Pause(),
        HighlightAnimation.Edge(EdgeHighlight("C2", "B1", true, startDelay = 1500L)),
        HighlightAnimation.Vertex(VertexHighlight("B1", true, startDelay = 1500L)),
        HighlightAnimation.Edge(EdgeHighlight("C2", "C3", true, startDelay = 1500L)),
        HighlightAnimation.Vertex(VertexHighlight("C3", true, startDelay = 1500L)),
        HighlightAnimation.Pause(),
        HighlightAnimation.Vertex(VertexHighlight("B1", true, startDelay = 2000L)),
        HighlightAnimation.Vertex(VertexHighlight("C12", true, startDelay = 2000L)),
        HighlightAnimation.Vertex(VertexHighlight("C3", true, startDelay = 2000L)),
    )
}

private fun createCaptureAnimations(): List<HighlightAnimation> {
    return listOf(
        HighlightAnimation.Vertex(VertexHighlight("C1", true, startDelay = 1500L)),
        HighlightAnimation.Pause(),
        HighlightAnimation.Edge(EdgeHighlight("C1", "B1", true, startDelay = 1500L)),
        HighlightAnimation.Vertex(VertexHighlight("B1", true, startDelay = 2000L)),
        HighlightAnimation.Pause(),
        HighlightAnimation.Vertex(VertexHighlight("A1", true, startDelay = 2000L)),
    )
}

private fun createUpgradeAnimations(): List<HighlightAnimation> {
    return listOf(
        HighlightAnimation.Vertex(VertexHighlight("B4", true, startDelay = 1500L)),
        HighlightAnimation.Pause(),
        HighlightAnimation.Edge(EdgeHighlight("B4", "C7", true, startDelay = 1500L)),
        HighlightAnimation.Vertex(VertexHighlight("C7", true, startDelay = 2000L)),
        HighlightAnimation.Pause(),
        HighlightAnimation.Edge(EdgeHighlight("B4", "C8", true, startDelay = 1500L)),
        HighlightAnimation.Vertex(VertexHighlight("C8", true, startDelay = 2000L)),
        HighlightAnimation.Pause(),
        HighlightAnimation.Vertex(VertexHighlight("C7", true, startDelay = 2000L)),
        HighlightAnimation.Vertex(VertexHighlight("C8", true, startDelay = 2000L))
    )
}

private fun createCastlingAnimations(): List<HighlightAnimation> {
    return listOf(
        HighlightAnimation.Vertex(VertexHighlight("C1", true, startDelay = 1500L)),
        HighlightAnimation.Vertex(VertexHighlight("C2", true, startDelay = 1500L)),
        HighlightAnimation.Pause(),
        HighlightAnimation.Vertex(VertexHighlight("B1", true, startDelay = 1500L)),
        HighlightAnimation.Pause(),
        HighlightAnimation.Edge(EdgeHighlight("C1", "C2", true, startDelay = 2000L)),
        HighlightAnimation.Pause(),
        HighlightAnimation.Vertex(VertexHighlight("B1", true, startDelay = 2000L))
    )
}