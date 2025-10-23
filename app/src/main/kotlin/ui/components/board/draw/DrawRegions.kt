package com.agustin.tarati.ui.components.board.draw

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.agustin.tarati.game.core.GameBoard.BoardRegion
import com.agustin.tarati.game.core.GameBoard.getVisualPosition
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.ui.components.board.animation.RegionHighlight
import kotlin.math.sin

fun DrawScope.drawRegionHighlight(
    highlight: RegionHighlight,
    canvasSize: Size,
    orientation: BoardOrientation,
) {
    val region = highlight.region

    // Crear path para la región
    val path = createRegionPath(region, canvasSize, orientation)

    // Efecto de pulso para el destello
    val pulseFactor = if (highlight.pulse) {
        val pulseTime = System.currentTimeMillis() % 1000L / 1000f
        (0.3f + 0.7f * sin(pulseTime * 2 * Math.PI).toFloat())
    } else {
        1f
    }

    // Dibujar fondo de la región con efecto de pulso
    drawPath(
        path = path,
        color = highlight.color.copy(alpha = 0.6f * pulseFactor),
        style = Fill
    )

    // Dibujar borde resaltado con efecto de pulso
    drawPath(
        path = path,
        color = highlight.color,
        style = Stroke(width = 3f * pulseFactor)
    )
}

private fun createRegionPath(
    region: BoardRegion,
    canvasSize: Size,
    orientation: BoardOrientation
): Path {
    return Path().apply {
        region.vertices.forEachIndexed { index, vertexId ->
            val pos = getVisualPosition(vertexId, canvasSize.width, canvasSize.height, orientation)
            if (index == 0) {
                moveTo(pos.x, pos.y)
            } else {
                lineTo(pos.x, pos.y)
            }
        }
        close()
    }
}