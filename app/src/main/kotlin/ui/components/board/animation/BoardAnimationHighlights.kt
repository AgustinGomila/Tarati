package com.agustin.tarati.ui.components.board.animation

import androidx.compose.ui.graphics.Color
import com.agustin.tarati.game.core.GameBoard.BoardRegion

data class VertexHighlight(
    val vertexId: String,
    val color: Color,
    val pulse: Boolean = false,
    val duration: Long = 500L,
    val startDelay: Long = 0L,
    val postDelay: Long = 0L,
    val persistent: Boolean = false,
    val messageResId: Int? = null
)

data class EdgeHighlight(
    val from: String,
    val to: String,
    val color: Color,
    val pulse: Boolean = false,
    val duration: Long = 500L,
    val startDelay: Long = 0L,
    val postDelay: Long = 0L,
    val persistent: Boolean = false,
    val messageResId: Int? = null
)

data class RegionHighlight(
    val region: BoardRegion,
    val color: Color,
    val pulse: Boolean = true,
    val duration: Long,
    val startDelay: Long = 0,
    val postDelay: Long = 0,
    val persistent: Boolean = false,
    val messageResId: Int? = null
)

sealed class HighlightAnimation {
    data class Vertex(val highlight: VertexHighlight) : HighlightAnimation()
    data class Edge(val highlight: EdgeHighlight) : HighlightAnimation()
    data class Region(val highlight: RegionHighlight) : HighlightAnimation()
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
                duration = 600L,
                startDelay = 100L
            )
        ),
        HighlightAnimation.Vertex(
            VertexHighlight(
                vertexId = to,
                color = Color(0xFF4CAF50),
                pulse = true,
                duration = 400L,
                startDelay = 300L
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
                duration = 600L
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
                duration = 600L
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
                duration = 400L
            )
        )
    }
}

fun createRegionHighlight(
    region: BoardRegion,
    duration: Long = 400L,
): HighlightAnimation {
    return HighlightAnimation.Region(
        RegionHighlight(
            region = region,
            color = Color(0xFFEFD47C),
            duration = duration
        )
    )
}