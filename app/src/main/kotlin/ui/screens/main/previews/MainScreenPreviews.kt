@file:Suppress("AssignedValueIsNeverRead")

package com.agustin.tarati.ui.screens.main.previews

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agustin.tarati.R
import com.agustin.tarati.game.ai.Difficulty
import com.agustin.tarati.game.core.CobColor
import com.agustin.tarati.game.core.CobColor.WHITE
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.core.initialGameState
import com.agustin.tarati.game.logic.toBoardOrientation
import com.agustin.tarati.ui.components.board.BoardEvents
import com.agustin.tarati.ui.components.board.CreateBoard
import com.agustin.tarati.ui.components.board.CreateBoardState
import com.agustin.tarati.ui.components.editor.EditActionEvents
import com.agustin.tarati.ui.components.editor.EditActionState
import com.agustin.tarati.ui.components.editor.EditColorEvents
import com.agustin.tarati.ui.components.editor.EditColorState
import com.agustin.tarati.ui.components.editor.PieceCounts
import com.agustin.tarati.ui.components.editor.previews.EditControlsPreview
import com.agustin.tarati.ui.components.sidebar.Sidebar
import com.agustin.tarati.ui.components.sidebar.SidebarEvents
import com.agustin.tarati.ui.components.sidebar.SidebarGameState
import com.agustin.tarati.ui.components.sidebar.SidebarUIState
import com.agustin.tarati.ui.components.topbar.TaratiTopBar
import com.agustin.tarati.ui.components.turnIndicator.IndicatorEvents
import com.agustin.tarati.ui.components.turnIndicator.TurnIndicator
import com.agustin.tarati.ui.components.turnIndicator.TurnIndicatorState
import com.agustin.tarati.ui.helpers.PreviewConfig
import com.agustin.tarati.ui.localization.LocalizedText
import com.agustin.tarati.ui.localization.localizedString
import com.agustin.tarati.ui.theme.TaratiTheme
import com.agustin.tarati.ui.theme.getBoardColors


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreenPreviewContent(
    config: PreviewConfig = PreviewConfig()
) {
    TaratiTheme(darkTheme = config.darkTheme) {
        val drawerState = rememberDrawerState(initialValue = config.drawerStateValue)
        val scope = rememberCoroutineScope()

        val exampleMoveHistory = listOf(
            Move("C1", "B1"),
            Move("C7", "B4"),
            Move("B1", "A1"),
            Move("B4", "A1")
        )

        // Estado del juego para el preview
        var gameState by remember { mutableStateOf(initialGameState()) }
        var playerSide by remember { mutableStateOf(config.playerSide) }
        var isEditing by remember { mutableStateOf(config.isEditing) }
        var isTutorial by remember { mutableStateOf(config.isTutorialActive) }
        var boardVisualState by remember { mutableStateOf(config.boardVisualState) }

        // Estado UI para el Sidebar
        var sidebarUIState by remember { mutableStateOf(SidebarUIState()) }

        // Implementación de eventos para el preview
        val sidebarEvents = createPreviewSidebarEvents(
            currentIsEditing = isEditing,
            onGameStateUpdate = { gameState = it },
            onPlayerSideUpdate = { playerSide = it },
            onEditingUpdate = { isEditing = it },
            debug = config.debug
        )

        // Crear el estado del juego para el Sidebar
        val sidebarGameState = SidebarGameState(
            gameState = gameState,
            playerSide = playerSide,
            moveIndex = 2,
            moveHistory = exampleMoveHistory,
            difficulty = Difficulty.DEFAULT,
            isAIEnabled = true,
        )

        // Estado y eventos para CreateBoard
        val createBoardState = CreateBoardState(
            gameState = gameState,
            lastMove = null,
            playerSide = playerSide,
            aiEnabled = true,
            isEditing = isEditing,
            isTutorialActive = isTutorial,
            isAIThinking = false,
            boardOrientation = toBoardOrientation(config.landScape, playerSide),
            editBoardOrientation = toBoardOrientation(config.landScape, playerSide),
            boardVisualState = boardVisualState
        )

        val createBoardEvents = createPreviewBoardEvents(config.debug)
        val indicatorState = TurnIndicatorState.HUMAN_TURN
        val indicatorEvents = createPreviewIndicatorEvents(config.debug)
        val boardColors = getBoardColors()

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                Sidebar(
                    modifier = Modifier.systemBarsPadding(),
                    sidebarState = sidebarGameState,
                    uiState = sidebarUIState,
                    events = sidebarEvents,
                    onUIStateChange = { newState -> sidebarUIState = newState }
                )
            }
        ) {
            Scaffold(
                topBar = {
                    TaratiTopBar(
                        scope = scope,
                        drawerState = drawerState,
                        title = localizedString(R.string.app_name),
                        isEditing = isEditing
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        if (!config.landScape && !isEditing) {
                            LocalizedText(
                                id = R.string.a_board_game_by_george_spencer_brown,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        CreateBoard(
                            modifier = Modifier.weight(1f),
                            state = createBoardState,
                            events = createBoardEvents,
                            boardColors = boardColors,
                            boardVisualState = boardVisualState,
                            tutorial = { },
                            content = {
                                EditControlsPreview(
                                    isLandscapeScreen = config.landScape,
                                    colorState = EditColorState(
                                        playerSide = playerSide,
                                        editColor = WHITE,
                                        editTurn = WHITE
                                    ),
                                    colorEvents = EditColorEvents(),
                                    actionState = EditActionState(
                                        pieceCounts = PieceCounts(4, 4),
                                        isValidDistribution = true,
                                        isCompletedDistribution = true
                                    ),
                                    actionEvents = EditActionEvents()
                                )
                            },
                            turnIndicator = {
                                TurnIndicator(
                                    modifier = it,
                                    state = indicatorState,
                                    currentTurn = gameState.currentTurn,
                                    boardColors = boardColors,
                                    indicatorEvents = indicatorEvents
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

// Previews Portrait
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun MainScreenPreview_WithDrawer_Portrait() {
    MainScreenPreviewContent(
        config = PreviewConfig(drawerStateValue = DrawerValue.Open)
    )
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun MainScreenPreview_WithDrawer_Portrait_Dark() {
    MainScreenPreviewContent(
        config = PreviewConfig(
            darkTheme = true,
            drawerStateValue = DrawerValue.Open
        )
    )
}

// Previews Landscape
@Preview(showBackground = true, device = "spec:width=891dp,height=411dp")
@Composable
fun MainScreenPreview_WithDrawer_Landscape() {
    MainScreenPreviewContent(
        config = PreviewConfig(
            drawerStateValue = DrawerValue.Open,
            landScape = true
        )
    )
}

@Preview(showBackground = true, device = "spec:width=891dp,height=411dp")
@Composable
fun MainScreenPreview_WithDrawer_Landscape_Dark() {
    MainScreenPreviewContent(
        config = PreviewConfig(
            darkTheme = true,
            drawerStateValue = DrawerValue.Open,
            landScape = true
        )
    )
}

// Previews con drawer cerrado (para ver el contenido principal)
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun MainScreenPreview_DrawerClosed_Portrait() {
    MainScreenPreviewContent(
        config = PreviewConfig(isEditing = true)
    )
}

@Preview(showBackground = true, device = "spec:width=891dp,height=411dp")
@Composable
fun MainScreenPreview_DrawerClosed_Landscape() {
    MainScreenPreviewContent(
        config = PreviewConfig(
            landScape = true,
            isEditing = true
        )
    )
}

// Preview adicional: Juego en progreso con más movimientos
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun MainScreenPreview_GameInProgress() {
    MainScreenPreviewContent(
        config = PreviewConfig(drawerStateValue = DrawerValue.Open)
    )
}

@Composable
private fun createPreviewSidebarEvents(
    currentIsEditing: Boolean,
    onGameStateUpdate: (GameState) -> Unit,
    onPlayerSideUpdate: (CobColor) -> Unit,
    onEditingUpdate: (Boolean) -> Unit,
    debug: Boolean
): SidebarEvents = object : SidebarEvents {
    override fun onMoveToCurrent() = onGameStateUpdate(initialGameState())
    override fun onUndo() {}
    override fun onRedo() {}
    override fun onDifficultyChange(difficulty: Difficulty) {
        if (debug) println("Difficulty changed to: $difficulty")
    }

    override fun onToggleAI() {
        if (debug) println("AI toggled")
    }

    override fun onSettings() {
        if (debug) println("Settings clicked")
    }

    override fun onNewGame(color: CobColor) {
        onPlayerSideUpdate(color)
        onGameStateUpdate(initialGameState())
        if (debug) println("New game with side: $color")
    }

    override fun onEditBoard() {
        onEditingUpdate(!currentIsEditing)
        if (debug) println("Edit board: $currentIsEditing")
    }

    override fun onAboutClick() {
        if (debug) println("About clicked")
    }
}

private fun createPreviewBoardEvents(debug: Boolean): BoardEvents = object : BoardEvents {
    override fun onMove(from: String, to: String) {
        if (debug) println("Move from $from to $to")
    }

    override fun onEditPiece(from: String) {
        if (debug) println("Edit piece at $from")
    }

    override fun onResetCompleted() {
        if (debug) println("Board reset completed")
    }
}

private fun createPreviewIndicatorEvents(debug: Boolean): IndicatorEvents = object : IndicatorEvents {
    override fun onTouch() {
        if (debug) println("Indicator turn clicked")
    }
}