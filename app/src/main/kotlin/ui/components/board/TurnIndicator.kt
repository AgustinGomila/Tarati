package com.agustin.tarati.ui.components.board

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
        Color.WHITE -> boardColors.whitePieceColor
        Color.BLACK -> boardColors.blackPieceColor
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
            .background(indicatorColor),
        contentAlignment = Alignment.Center
    ) {
        if (isAIThinking) {
            DrawLogo(
                size = size,
                rotation = rotation
            )
        } else {
            DrawLogoStatic(size = size)
        }
    }
}

@Composable
fun DrawLogo(size: Dp = 60.dp, rotation: Float) {
    Box(
        modifier = Modifier
            .size(size * 0.6f)
            .graphicsLayer {
                rotationZ = rotation
            }
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = localizedString(R.string.ai_thinking),
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
fun DrawLogoStatic(size: Dp = 60.dp) {
    Box(
        modifier = Modifier
            .size(size * 0.6f)
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = localizedString(R.string.human_turn),
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TurnIndicatorPreview_Thinking() {
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
                currentTurn = Color.WHITE,
                size = 80.dp,
                isAIThinking = true
            )

            TurnIndicator(
                currentTurn = Color.BLACK,
                size = 80.dp,
                isAIThinking = true
            )

            LocalizedText(R.string.human_turn, style = MaterialTheme.typography.titleMedium)

            TurnIndicator(
                currentTurn = Color.WHITE,
                size = 80.dp,
                isAIThinking = false
            )

            TurnIndicator(
                currentTurn = Color.BLACK,
                size = 80.dp,
                isAIThinking = false
            )
        }
    }
}