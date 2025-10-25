package com.agustin.tarati.ui.components.board.draw

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.agustin.tarati.game.core.Cob
import com.agustin.tarati.game.core.CobColor.BLACK
import com.agustin.tarati.game.core.CobColor.WHITE
import com.agustin.tarati.ui.components.board.animation.AnimatedCob
import com.agustin.tarati.ui.theme.BoardColors


fun DrawScope.drawCob(
    selectedVertexId: String?,
    vertexId: String,
    cob: Cob,
    colors: BoardColors,
    sizeFactor: Float = 1.2f,
) {
    // Dibujar borde + relleno
    val center = Offset(size.width / 2f, size.height / 2f)
    val baseRadius = minOf(size.width, size.height) / 2f * sizeFactor

    val (pieceColor, borderColor) = when (cob.color) {
        WHITE -> colors.whiteCobColor to colors.whiteCobBorderColor
        BLACK -> colors.blackCobColor to colors.blackCobBorderColor
    }

    drawCircle(color = pieceColor, center = center, radius = baseRadius)
    drawCircle(color = borderColor, center = center, radius = baseRadius, style = Stroke(width = 16f))

    if (cob.isUpgraded) {
        val upgradeColor = if (cob.color == WHITE)
            colors.blackCobColor else colors.whiteCobColor

        drawCircle(
            color = upgradeColor,
            center = center,
            radius = baseRadius * 0.2f
        )
    }

    // Resaltado de selección
    if (vertexId == selectedVertexId) {
        drawCircle(
            color = colors.selectionIndicatorColor,
            center = center,
            radius = baseRadius * 1.2f,
            style = Stroke(width = 3f),
            alpha = 0.7f,
        )
    }
}

fun DrawScope.drawAnimatedCob(
    selectedVertexId: String?,
    vertexId: String,
    animatedCob: AnimatedCob,
    colors: BoardColors,
    sizeFactor: Float = 1.2f,
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val baseRadius = minOf(size.width, size.height) / 2f * sizeFactor

    // Determinar colores actuales considerando animaciones
    val currentCob = animatedCob.cob
    val (pieceColor, borderColor) = when (currentCob.color) {
        WHITE -> colors.whiteCobColor to colors.whiteCobBorderColor
        BLACK -> colors.blackCobColor to colors.blackCobBorderColor
    }

    val invertedColor = when (currentCob.color) {
        WHITE -> colors.blackCobColor
        BLACK -> colors.whiteCobColor
    }

    // Dibujar pieza base
    drawCircle(color = pieceColor, center = center, radius = baseRadius)
    drawCircle(color = borderColor, center = center, radius = baseRadius, style = Stroke(width = 16f))

    // Animación de upgrade - círculo interno
    if (currentCob.isUpgraded) {
        val upgradeAlpha = animatedCob.upgradeProgress
        if (upgradeAlpha > 0f) {
            // Punto central de upgrade
            drawCircle(
                color = invertedColor.copy(alpha = upgradeAlpha),
                center = center,
                radius = baseRadius * 0.2f * upgradeAlpha,
            )
        }
    }

    // Animación de conversión - efecto de inversión
    if (animatedCob.isConverting) {
        val conversionAlpha = animatedCob.conversionProgress

        // Efecto de aura durante la conversión
        if (conversionAlpha < 0.8f) {
            val auraAlpha = (1f - conversionAlpha) * 0.4f
            drawCircle(
                color = colors.selectionIndicatorColor.copy(alpha = auraAlpha),
                center = center,
                radius = baseRadius * 1.3f,
                alpha = auraAlpha * 0.5f,
            )
        }

        // Efecto de parpadeo
        if (conversionAlpha % 0.3f < 0.15f) {
            val flashAlpha = (1f - conversionAlpha) * 0.6f
            drawCircle(
                color = invertedColor.copy(alpha = flashAlpha),
                center = center,
                radius = baseRadius * 1.1f,
                style = Stroke(width = 2f),
                alpha = flashAlpha,
            )
        }
    }

    // Resaltado de selección
    if (vertexId == selectedVertexId) {
        drawCircle(
            color = colors.selectionIndicatorColor,
            center = center,
            radius = baseRadius * 1.2f,
            style = Stroke(width = 3f)
        )
    }
}