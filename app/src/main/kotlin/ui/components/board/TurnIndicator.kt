package com.agustin.tarati.ui.components.board

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.agustin.tarati.R
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.ui.localization.LocalizedText
import com.agustin.tarati.ui.theme.AppColors.getBoardColors
import com.agustin.tarati.ui.theme.TaratiTheme

@Composable
fun TurnIndicator(
    modifier: Modifier = Modifier,
    currentTurn: Color,
    size: Dp = 60.dp,
    isAIThinking: Boolean = false
) {
    val boardColors = getBoardColors()

    val indicatorColor = when (currentTurn) {
        WHITE -> boardColors.whitePieceColor
        BLACK -> boardColors.blackPieceColor
    }

    val borderColor = when (currentTurn) {
        WHITE -> boardColors.whitePieceBorderColor
        BLACK -> boardColors.blackPieceBorderColor
    }

    // Múltiples animaciones para efectos más elaborados
    val infiniteTransition = rememberInfiniteTransition()

    // Animación de pulso para el círculo principal
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TurnIndicatorPulse"
    )

    // Animación de rotación para el spinner
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "TurnIndicatorRotation"
    )

    // Animación de opacidad parpadeante
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TurnIndicatorBlink"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = if (isAIThinking) pulseScale else 1f
                scaleY = if (isAIThinking) pulseScale else 1f
            }
            .size(size)
            .padding(1.dp)
            .clip(CircleShape)
            .background(indicatorColor),
        contentAlignment = Alignment.Center
    ) {
        // Efectos visuales cuando la IA está pensando
        if (isAIThinking) {
            // Spinner giratorio
            Box(
                modifier = Modifier
                    .size(size * 0.7f)
                    .graphicsLayer {
                        rotationZ = rotation
                    }
                    .background(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Transparent,
                                when (currentTurn) {
                                    WHITE -> boardColors.blackPieceColor.copy(alpha = blinkAlpha)
                                    BLACK -> boardColors.whitePieceColor.copy(alpha = blinkAlpha)
                                },
                                Transparent
                            )
                        ),
                        shape = CircleShape
                    )
                    .clip(CircleShape)
            )

            // Punto central
            Box(
                modifier = Modifier
                    .size(size * 0.2f)
                    .clip(CircleShape)
                    .background(
                        when (currentTurn) {
                            WHITE -> boardColors.blackPieceColor
                            BLACK -> boardColors.whitePieceColor
                        }
                    )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TurnIndicatorPreview_White() {
    MaterialTheme {
        TurnIndicator(currentTurn = WHITE)
    }
}

@Preview(showBackground = true)
@Composable
fun TurnIndicatorPreview_Black() {
    MaterialTheme {
        TurnIndicator(currentTurn = BLACK)
    }
}

@Preview(showBackground = true)
@Composable
fun TurnIndicatorPreview_Both() {
    MaterialTheme {
        Row {
            TurnIndicator(currentTurn = WHITE)
            TurnIndicator(currentTurn = BLACK)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TurnIndicatorPreview_White_Thinking() {
    TaratiTheme {
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            TurnIndicator(
                currentTurn = WHITE,
                isAIThinking = true
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TurnIndicatorPreview_Black_Thinking() {
    TaratiTheme {
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            TurnIndicator(
                currentTurn = BLACK,
                isAIThinking = true
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TurnIndicatorPreview_White_Normal() {
    TaratiTheme {
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            TurnIndicator(
                currentTurn = WHITE,
                isAIThinking = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TurnIndicatorPreview_Black_Normal() {
    TaratiTheme {
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            TurnIndicator(
                currentTurn = BLACK,
                isAIThinking = false
            )
        }
    }
}

// Preview que muestra ambos estados
@Preview(showBackground = true, widthDp = 400, heightDp = 200)
@Composable
fun TurnIndicatorPreview_BothStates() {
    TaratiTheme {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LocalizedText(R.string.human_turn, style = MaterialTheme.typography.labelMedium)
                TurnIndicator(
                    currentTurn = WHITE,
                    isAIThinking = false
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LocalizedText(R.string.ai_thinking, style = MaterialTheme.typography.labelMedium)
                TurnIndicator(
                    currentTurn = WHITE,
                    isAIThinking = true
                )
            }
        }
    }
}