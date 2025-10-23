package com.agustin.tarati.ui.components.turnIndicator

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.agustin.tarati.R
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.ui.localization.LocalizedText
import com.agustin.tarati.ui.localization.localizedString
import com.agustin.tarati.ui.theme.BoardColors
import com.agustin.tarati.ui.theme.TaratiTheme
import com.agustin.tarati.ui.theme.getBoardColors

enum class TurnIndicatorState {
    AI_THINKING,
    HUMAN_TURN,
    NEUTRAL
}

interface IndicatorEvents {
    fun onTouch()
}

@Composable
fun TurnIndicator(
    modifier: Modifier = Modifier,
    state: TurnIndicatorState,
    currentTurn: Color,
    size: Dp = 60.dp,
    boardColors: BoardColors,
    indicatorEvents: IndicatorEvents,
) {
    // Determinar color, icono y comportamiento según el estado
    val color = when (currentTurn) {
        Color.WHITE -> boardColors.whiteCobColor
        Color.BLACK -> boardColors.blackCobColor
    }

    val (indicatorColor, isClickable, contentDescription) = when (state) {
        TurnIndicatorState.AI_THINKING -> Triple(
            first = color,
            second = false,
            third = localizedString(R.string.ai_thinking)
        )

        TurnIndicatorState.HUMAN_TURN -> Triple(
            first = color,
            second = false,
            third = localizedString(R.string.human_turn)
        )

        TurnIndicatorState.NEUTRAL -> Triple(
            first = boardColors.neutralColor,
            second = true,
            third = localizedString(R.string.new_game)
        )
    }

    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "TurnIndicatorRotation"
    )

    Box(
        modifier = modifier
            .padding(8.dp)
            .size(size)
            .clip(CircleShape)
            .background(indicatorColor)
            .clickable(isClickable) {
                if (isClickable) indicatorEvents.onTouch()
            },
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            TurnIndicatorState.AI_THINKING -> DrawLogo(
                size = size,
                rotation = rotation,
                contentDescription = contentDescription
            )

            TurnIndicatorState.HUMAN_TURN -> DrawLogoStatic(
                size = size,
                contentDescription = contentDescription
            )

            TurnIndicatorState.NEUTRAL -> DrawNewGameIcon(
                size = size,
                contentDescription = contentDescription
            )
        }
    }
}

@Composable
fun DrawLogo(size: Dp = 60.dp, rotation: Float, contentDescription: String) {
    Box(
        modifier = Modifier
            .size(size * 0.6f)
            .graphicsLayer {
                rotationZ = rotation
            }
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
fun DrawLogoStatic(size: Dp = 60.dp, contentDescription: String) {
    Box(
        modifier = Modifier
            .size(size * 0.6f)
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
fun DrawNewGameIcon(size: Dp = 60.dp, contentDescription: String) {
    Box(
        modifier = Modifier
            .size(size * 0.6f)
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

fun createPreviewIndicatorEvents(): IndicatorEvents = object : IndicatorEvents {
    override fun onTouch() = Unit
}

@Preview(showBackground = true)
@Composable
fun TurnIndicatorPreview_AllStates() {
    val boardColors = getBoardColors()

    TaratiTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LocalizedText(R.string.ai_thinking, style = MaterialTheme.typography.titleMedium)

            TurnIndicator(
                state = TurnIndicatorState.AI_THINKING,
                currentTurn = Color.BLACK,
                size = 80.dp,
                boardColors = boardColors,
                indicatorEvents = createPreviewIndicatorEvents()
            )

            LocalizedText(R.string.human_turn, style = MaterialTheme.typography.titleMedium)

            TurnIndicator(
                state = TurnIndicatorState.HUMAN_TURN,
                currentTurn = Color.WHITE,
                size = 80.dp,
                boardColors = boardColors,
                indicatorEvents = createPreviewIndicatorEvents()
            )

            LocalizedText(R.string.human_turn, style = MaterialTheme.typography.titleMedium)

            TurnIndicator(
                state = TurnIndicatorState.HUMAN_TURN,
                currentTurn = Color.BLACK,
                size = 80.dp,
                boardColors = boardColors,
                indicatorEvents = createPreviewIndicatorEvents()
            )

            LocalizedText(R.string.new_game, style = MaterialTheme.typography.titleMedium)

            TurnIndicator(
                state = TurnIndicatorState.NEUTRAL,
                currentTurn = Color.BLACK,
                size = 80.dp,
                boardColors = boardColors,
                indicatorEvents = createPreviewIndicatorEvents()
            )
        }
    }
}