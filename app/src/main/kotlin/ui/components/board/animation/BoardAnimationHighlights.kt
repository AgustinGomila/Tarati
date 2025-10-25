package com.agustin.tarati.ui.components.board.animation

import com.agustin.tarati.game.core.GameBoard.BoardRegion

data class VertexHighlight(
    val vertexId: String,
    val pulse: Boolean = false,
    val duration: Long = 500L,
    val startDelay: Long = 0L,
    val postDelay: Long = 0L,
    val persistent: Boolean = false,
    val action: HighlightAction = HighlightAction.MOVE,
    val messageResId: Int? = null
)

data class EdgeHighlight(
    val from: String,
    val to: String,
    val pulse: Boolean = false,
    val duration: Long = 500L,
    val startDelay: Long = 0L,
    val postDelay: Long = 0L,
    val persistent: Boolean = false,
    val action: HighlightAction = HighlightAction.MOVE,
    val messageResId: Int? = null
)

data class RegionHighlight(
    val region: BoardRegion,
    val pulse: Boolean = true,
    val duration: Long,
    val startDelay: Long = 0,
    val postDelay: Long = 0,
    val persistent: Boolean = false,
    val action: HighlightAction = HighlightAction.MOVE,
    val messageResId: Int? = null
)

enum class HighlightAction {
    MOVE,
    CAPTURE,
    UPGRADE
}

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
                pulse = true,
                duration = 600L,
                startDelay = 200L
            )
        ),
        HighlightAnimation.Vertex(
            VertexHighlight(
                vertexId = to,
                pulse = true,
                duration = 600L,
                startDelay = 400L
            )
        )
    )
}

fun createCaptureHighlight(vertexId: String): List<HighlightAnimation> {
    return listOf(
        HighlightAnimation.Vertex(
            VertexHighlight(
                vertexId = vertexId,
                pulse = true,
                duration = 600L,
                action = HighlightAction.CAPTURE,
            )
        )
    )
}

fun createUpgradeHighlight(vertexId: String): List<HighlightAnimation> {
    return listOf(
        HighlightAnimation.Vertex(
            VertexHighlight(
                vertexId = vertexId,
                pulse = true,
                duration = 600L,
                action = HighlightAction.UPGRADE,
            )
        )
    )
}

fun createValidMovesHighlights(validMoves: List<String>): List<HighlightAnimation> {
    return validMoves.map { vertexId ->
        HighlightAnimation.Vertex(
            VertexHighlight(
                vertexId = vertexId,
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
            duration = duration
        )
    )
}