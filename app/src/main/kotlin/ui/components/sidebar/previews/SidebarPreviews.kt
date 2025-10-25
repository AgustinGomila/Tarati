package com.agustin.tarati.ui.components.sidebar.previews

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.agustin.tarati.game.ai.Difficulty
import com.agustin.tarati.game.core.CobColor
import com.agustin.tarati.game.core.CobColor.BLACK
import com.agustin.tarati.game.core.CobColor.WHITE
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.ui.components.sidebar.Sidebar
import com.agustin.tarati.ui.components.sidebar.SidebarEvents
import com.agustin.tarati.ui.components.sidebar.SidebarGameState
import com.agustin.tarati.ui.components.sidebar.SidebarUIState
import com.agustin.tarati.ui.helpers.customGameState


@Preview(showBackground = true, widthDp = 280, heightDp = 800)
@Composable
fun SidebarPreview() {
    MaterialTheme {
        val exampleGameState = customGameState()
        val exampleMoveHistory = listOf(
            Move("C1", "B1"),
            Move("C7", "B4"),
            Move("B1", "A1"),
            Move("B4", "A1")
        )

        val sidebarGameState = SidebarGameState(
            gameState = exampleGameState,
            playerSide = WHITE,
            moveIndex = 2,
            moveHistory = exampleMoveHistory,
            difficulty = Difficulty.DEFAULT,
            isAIEnabled = true,
        )

        Sidebar(
            sidebarState = sidebarGameState,
            events = PreviewSidebarEvents()
        )
    }
}

@Preview(showBackground = true, widthDp = 280, heightDp = 800)
@Composable
fun SidebarPreview_Dark() {
    MaterialTheme(
        colorScheme = darkColorScheme()
    ) {
        val exampleGameState = customGameState()
        val exampleMoveHistory = listOf(
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
        )
        val sidebarGameState = SidebarGameState(
            gameState = exampleGameState,
            playerSide = BLACK,
            moveIndex = 1,
            moveHistory = exampleMoveHistory,
            difficulty = Difficulty.HARD,
            isAIEnabled = false,
        )

        Sidebar(
            sidebarState = sidebarGameState,
            events = PreviewSidebarEvents()
        )
    }
}

@Preview(showBackground = true, widthDp = 280, heightDp = 800)
@Composable
fun SidebarPreview_ExpandedDropdown() {
    MaterialTheme {
        val exampleGameState = customGameState()
        val exampleMoveHistory = listOf(
            Move("C1", "B1"),
            Move("C7", "B4")
        )

        val sidebarGameState = SidebarGameState(
            gameState = exampleGameState,
            playerSide = WHITE,
            moveIndex = 1,
            moveHistory = exampleMoveHistory,
            difficulty = Difficulty.DEFAULT,
            isAIEnabled = true,
        )

        var uiState by remember { mutableStateOf(SidebarUIState(isDifficultyExpanded = true)) }

        Sidebar(
            sidebarState = sidebarGameState,
            uiState = uiState,
            events = PreviewSidebarEvents(),
            onUIStateChange = { uiState = it }
        )
    }
}

class PreviewSidebarEvents : SidebarEvents {
    override fun onMoveToCurrent() {}
    override fun onUndo() {}
    override fun onRedo() {}
    override fun onDifficultyChange(difficulty: Difficulty) {}
    override fun onToggleAI() {}
    override fun onSettings() {}
    override fun onNewGame(color: CobColor) {}
    override fun onEditBoard() {}
    override fun onAboutClick() {}
}