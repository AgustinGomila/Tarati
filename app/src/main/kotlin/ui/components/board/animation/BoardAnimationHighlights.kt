package com.agustin.tarati.ui.components.board.animation

import androidx.compose.ui.graphics.Color

data class VertexHighlight(
    val vertexId: String,
    val color: Color,
    val pulse: Boolean = false,
    val duration: Long = 500L,
    val messageResId: Int? = null
)

data class EdgeHighlight(
    val from: String,
    val to: String,
    val color: Color,
    val pulse: Boolean = false,
    val duration: Long = 500L
)

sealed class HighlightAnimation {
    data class Vertex(val highlight: VertexHighlight) : HighlightAnimation()
    data class Edge(val highlight: EdgeHighlight) : HighlightAnimation()
    data class Pause(val duration: Long = 500L) : HighlightAnimation()
}

fun createMoveHighlight(from: String, to: String): List<HighlightAnimation> {
    return listOf(
        HighlightAnimation.Edge(
            EdgeHighlight(
                from = from,
                to = to,
                color = Color(0xFFDED760),
                pulse = true,
                duration = 500L
            )
        ),
        HighlightAnimation.Vertex(
            VertexHighlight(
                vertexId = to,
                color = Color(0xFF4CAF50),
                pulse = true,
                duration = 500L
            )
        )
    )
}

fun createCaptureHighlight(vertexId: String): List<HighlightAnimation> {
    return listOf(
        HighlightAnimation.Vertex(
            VertexHighlight(
                vertexId = vertexId,
                color = Color(0xFFF44336),
                pulse = true,
                duration = 1000L
            )
        )
    )
}

fun createUpgradeHighlight(vertexId: String): List<HighlightAnimation> {
    return listOf(
        HighlightAnimation.Vertex(
            VertexHighlight(
                vertexId = vertexId,
                color = Color(0xFF36B8F4),
                pulse = true,
                duration = 1000L
            )
        )
    )
}

fun createValidMovesHighlights(validMoves: List<String>): List<HighlightAnimation> {
    return validMoves.map { vertexId ->
        HighlightAnimation.Vertex(
            VertexHighlight(
                vertexId = vertexId,
                color = Color(0xFFF3A621),
                pulse = false,
                duration = 500L
            )
        )
    }
}

fun createTutorialSequence(): List<HighlightAnimation> {
    return listOf(
        HighlightAnimation.Vertex(
            VertexHighlight(
                vertexId = "C2",
                color = Color.Yellow,
                pulse = true,
                duration = 1500L,
                messageResId = null
            )
        ),
        HighlightAnimation.Pause(800L), // Pausa más larga entre elementos
        HighlightAnimation.Edge(
            EdgeHighlight(
                from = "C2",
                to = "B1",
                color = Color.Yellow,
                pulse = true,
                duration = 1500L
            )
        ),
        HighlightAnimation.Pause(800L),
        HighlightAnimation.Vertex(
            VertexHighlight(
                vertexId = "B1",
                color = Color.Yellow,
                pulse = true,
                duration = 1500L
            )
        )
    )
}