package com.agustin.tarati.ui.components.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.agustin.tarati.ui.theme.AppColors.getBoardColors
import com.agustin.tarati.game.core.Color as SideColor

@Composable
fun TurnIndicator(
    modifier: Modifier = Modifier,
    currentTurn: SideColor,
    size: Dp = 60.dp
) {
    val indicatorColor = when (currentTurn) {
        SideColor.WHITE -> getBoardColors().whitePieceColor
        SideColor.BLACK -> getBoardColors().blackPieceColor
    }

    val borderColor = when (currentTurn) {
        SideColor.WHITE -> getBoardColors().whitePieceBorderColor
        SideColor.BLACK -> getBoardColors().blackPieceBorderColor
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(borderColor)
            .padding(1.dp)
            .clip(CircleShape)
            .background(indicatorColor)
    )
}

@Preview(showBackground = true)
@Composable
fun TurnIndicatorPreview_White() {
    MaterialTheme {
        TurnIndicator(currentTurn = SideColor.WHITE, size = 60.dp)
    }
}

@Preview(showBackground = true)
@Composable
fun TurnIndicatorPreview_Black() {
    MaterialTheme {
        TurnIndicator(currentTurn = SideColor.BLACK, size = 60.dp)
    }
}

@Preview(showBackground = true)
@Composable
fun TurnIndicatorPreview_Both() {
    MaterialTheme {
        Row {
            TurnIndicator(currentTurn = SideColor.WHITE, size = 60.dp)
            TurnIndicator(currentTurn = SideColor.BLACK, size = 60.dp)
        }
    }
}