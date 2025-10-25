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
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agustin.tarati.R
import com.agustin.tarati.game.ai.Difficulty
import com.agustin.tarati.game.core.CobColor
import com.agustin.tarati.game.core.CobColor.BLACK
import com.agustin.tarati.game.core.CobColor.WHITE
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.core.initialGameState
import com.agustin.tarati.game.core.opponent
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.game.logic.toBoardOrientation
import com.agustin.tarati.ui.components.board.Board
import com.agustin.tarati.ui.components.board.BoardEvents
import com.agustin.tarati.ui.components.board.BoardState
import com.agustin.tarati.ui.components.sidebar.Sidebar
import com.agustin.tarati.ui.components.sidebar.SidebarEvents
import com.agustin.tarati.ui.components.sidebar.SidebarGameState
import com.agustin.tarati.ui.components.sidebar.SidebarUIState
import com.agustin.tarati.ui.components.turnIndicator.IndicatorEvents
import com.agustin.tarati.ui.components.turnIndicator.TurnIndicator
import com.agustin.tarati.ui.components.turnIndicator.TurnIndicatorState
import com.agustin.tarati.ui.helpers.PreviewConfig
import com.agustin.tarati.ui.localization.LocalizedText
import com.agustin.tarati.ui.screens.main.BottomControls
import com.agustin.tarati.ui.screens.main.CreateBoard
import com.agustin.tarati.ui.screens.main.CreateBoardState
import com.agustin.tarati.ui.screens.main.EditActionEvents
import com.agustin.tarati.ui.screens.main.EditActionState
import com.agustin.tarati.ui.screens.main.EditColorEvents
import com.agustin.tarati.ui.screens.main.EditColorState
import com.agustin.tarati.ui.screens.main.LeftControls
import com.agustin.tarati.ui.screens.main.PieceCounts
import com.agustin.tarati.ui.screens.main.RightControls
import com.agustin.tarati.ui.screens.main.TaratiTopBar
import com.agustin.tarati.ui.screens.main.TopControls
import com.agustin.tarati.ui.screens.settings.BoardVisualState
import com.agustin.tarati.ui.theme.TaratiTheme
import com.agustin.tarati.ui.theme.getBoardColors


@Preview(showBackground = true)
@Composable
fun EditingModePreviewContent(
    darkTheme: Boolean = false,
    isLandscape: Boolean = false,
    boardOrientation: BoardOrientation = if (isLandscape) BoardOrientation.LANDSCAPE_BLACK else BoardOrientation.PORTRAIT_WHITE
) {
    TaratiTheme(darkTheme = darkTheme) {
        val exampleGameState = initialGameState()

        var isEditing by remember { mutableStateOf(true) }
        var aiEnabled by remember { mutableStateOf(false) }
        var editColor by remember { mutableStateOf(WHITE) }
        var editTurn by remember { mutableStateOf(WHITE) }
        var playerSide by remember { mutableStateOf(WHITE) }

        val pieceCounts = PieceCounts(4, 4)
        val isValidDistribution = true
        val isCompletedDistribution = true

        // Crear estado para Board
        val boardState = BoardState(
            gameState = exampleGameState,
            lastMove = null,
            aiEnabled = aiEnabled,
            boardOrientation = boardOrientation,
            boardVisualState = BoardVisualState(
                labelsVisibles = false,
                verticesVisibles = true,
                edgesVisibles = true,
            ),
            isEditing = isEditing,
        )

        // Crear eventos para Board
        val boardEvents = createPreviewBoardEvents(false)
        val boardColors = getBoardColors()

        Box(modifier = Modifier.fillMaxSize()) {
            Board(
                modifier = Modifier.fillMaxSize(),
                playerSide = playerSide,
                boardState = boardState,
                boardColors = boardColors,
                events = boardEvents,
            )

            EditControlsPreview(
                isLandscapeScreen = isLandscape,
                colorState = EditColorState(
                    playerSide = playerSide,
                    editColor = editColor,
                    editTurn = editTurn
                ),
                colorEvents = EditColorEvents(
                    onColorToggle = { editColor = editColor.opponent() },
                    onPlayerSideToggle = { playerSide = playerSide.opponent() },
                    onTurnToggle = { editTurn = editTurn.opponent() }
                ),
                actionState = EditActionState(
                    pieceCounts = pieceCounts,
                    isValidDistribution = isValidDistribution,
                    isCompletedDistribution = isCompletedDistribution
                ),
                actionEvents = EditActionEvents(
                    onClearBoard = { /* No-op en preview */ }
                )
            )
        }
    }
}

// Función base reutilizable para los previews
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

// Previews de EditControls actualizados
@Preview(showBackground = true, widthDp = 400, heightDp = 200)
@Composable
fun EditControlsPreview_Portrait() {
    TaratiTheme {
        EditControlsPreview(isLandscapeScreen = false)
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 200)
@Composable
fun EditControlsPreview_Landscape() {
    TaratiTheme {
        EditControlsPreview(isLandscapeScreen = true)
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 200)
@Composable
fun EditControlsPreview_InvalidDistribution() {
    TaratiTheme {
        EditControlsPreview(
            isLandscapeScreen = false,
            actionState = EditActionState(
                pieceCounts = PieceCounts(8, 0),
                isValidDistribution = false,
                isCompletedDistribution = false
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 200)
@Composable
fun EditControlsPreview_CompletedDistribution() {
    TaratiTheme {
        EditControlsPreview(
            isLandscapeScreen = false,
            actionState = EditActionState(
                pieceCounts = PieceCounts(7, 1),
                isValidDistribution = true,
                isCompletedDistribution = true
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 200)
@Composable
fun EditControlsPreview_BlackColor() {
    TaratiTheme {
        EditControlsPreview(
            isLandscapeScreen = false,
            colorState = EditColorState(
                editColor = BLACK,
                playerSide = BLACK,
                editTurn = BLACK
            )
        )
    }
}

@Composable
fun EditControlsPreview(
    isLandscapeScreen: Boolean,
    colorState: EditColorState = EditColorState(),
    colorEvents: EditColorEvents = EditColorEvents(),
    actionState: EditActionState = EditActionState(),
    actionEvents: EditActionEvents = EditActionEvents()
) {
    if (isLandscapeScreen) {
        // Landscape: controles a izquierda y derecha
        Box(modifier = Modifier.fillMaxSize()) {
            LeftControls(
                modifier = Modifier.align(CenterStart),
                state = colorState,
                events = colorEvents
            )

            RightControls(
                modifier = Modifier.align(CenterEnd),
                state = actionState,
                events = actionEvents
            )
        }
    } else {
        // Portrait: controles en superior e inferior
        Box(modifier = Modifier.fillMaxSize()) {
            TopControls(
                modifier = Modifier.align(TopCenter),
                state = colorState,
                events = colorEvents
            )

            BottomControls(
                modifier = Modifier.align(BottomCenter),
                state = actionState,
                events = actionEvents
            )
        }
    }
}

// Previews para componentes individuales
@Preview(showBackground = true, widthDp = 200, heightDp = 300)
@Composable
fun LeftControlsPreview() {
    TaratiTheme {
        LeftControls(
            state = EditColorState(
                playerSide = WHITE,
                editColor = WHITE,
                editTurn = WHITE
            ),
            events = EditColorEvents()
        )
    }
}

@Preview(showBackground = true, widthDp = 200, heightDp = 300)
@Composable
fun RightControlsPreview() {
    TaratiTheme {
        RightControls(
            state = EditActionState(
                pieceCounts = PieceCounts(4, 4),
                isValidDistribution = true,
                isCompletedDistribution = true
            ),
            events = EditActionEvents()
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 100)
@Composable
fun TopControlsPreview() {
    TaratiTheme {
        TopControls(
            state = EditColorState(
                playerSide = WHITE,
                editColor = WHITE,
                editTurn = WHITE
            ),
            events = EditColorEvents()
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 100)
@Composable
fun BottomControlsPreview() {
    TaratiTheme {
        BottomControls(
            state = EditActionState(
                pieceCounts = PieceCounts(4, 4),
                isValidDistribution = true,
                isCompletedDistribution = true
            ),
            events = EditActionEvents()
        )
    }
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