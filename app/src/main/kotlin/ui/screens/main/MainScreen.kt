package com.agustin.tarati.ui.screens.main

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.agustin.tarati.R
import com.agustin.tarati.game.ai.Difficulty
import com.agustin.tarati.game.ai.TaratiAI.applyMoveToBoard
import com.agustin.tarati.game.ai.TaratiAI.getNextBestMove
import com.agustin.tarati.game.ai.TaratiAI.isGameOver
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.ui.components.board.Board
import com.agustin.tarati.ui.components.board.TurnIndicator
import com.agustin.tarati.ui.components.sidebar.Sidebar
import com.agustin.tarati.ui.localization.LocalizedText
import com.agustin.tarati.ui.navigation.ScreenDestinations.SettingsScreenDest
import com.agustin.tarati.ui.screens.main.MainViewModel.Companion.initialGameState
import com.agustin.tarati.ui.theme.TaratiTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscapeScreen = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // ViewModel que guarda estado, historial y dificultad
    val viewModel: MainViewModel = viewModel()

    val vmGameState by viewModel.gameState.collectAsState(initialGameState())
    val vmHistory by viewModel.history.collectAsState(emptyList())
    val vmDifficulty by viewModel.difficulty.collectAsState(Difficulty.DEFAULT)
    val vmMoveIndex by viewModel.moveIndex.collectAsState(-1)
    val vmAIEnabled by viewModel.aIEnabled.collectAsState(true)
    val vmPlayerSide by viewModel.playerSide.collectAsState(WHITE)

    var stopAI by remember { mutableStateOf(false) }
    var resetBoard by remember { mutableStateOf(false) }

    // Estados para diálogos
    var showGameOverDialog by remember { mutableStateOf(false) }
    var showNewGameDialog by remember { mutableStateOf(false) }
    var gameOverMessage by remember { mutableStateOf("") }
    var showAboutDialog by remember { mutableStateOf(false) }

    fun applyMove(from: String, to: String) {
        stopAI = false

        println("Applying move: $from -> $to")

        val newBoardState = applyMoveToBoard(vmGameState, from, to)

        val nextTurn = if (vmGameState.currentTurn == WHITE) BLACK else WHITE
        val nextState = newBoardState.copy(currentTurn = nextTurn)

        val newEntry = Pair(Move(from, to), nextState)
        val truncated =
            if (vmMoveIndex < vmHistory.size - 1) {
                vmHistory.take(vmMoveIndex + 1)
            } else vmHistory

        val newMoveHistory = truncated + newEntry
        viewModel.updateMoveIndex(newMoveHistory.size - 1)

        viewModel.updateHistory(newMoveHistory)
        viewModel.updateGameState(nextState)

        if (isGameOver(nextState)) {
            val winner = if (nextState.currentTurn == WHITE) BLACK else WHITE
            val winnerName = if (winner == WHITE)
                context.getString(R.string.white)
            else
                context.getString(R.string.black)

            gameOverMessage = context.getString(
                R.string.game_over_wins,
                winnerName
            )
            showGameOverDialog = true
            stopAI = true
        }
    }

    LaunchedEffect(vmGameState.currentTurn, vmAIEnabled, stopAI, vmDifficulty, vmPlayerSide) {
        if (!vmAIEnabled || stopAI) return@LaunchedEffect

        // La IA juega cuando:
        // 1. Está habilitada
        // 2. No es el turno del jugador humano
        // 3. El juego no está terminado
        val shouldAIPlay = vmGameState.currentTurn != vmPlayerSide &&
                !isGameOver(vmGameState)

        println("AI Check: enabled=${true}, playerSide=$vmPlayerSide, currentTurn=${vmGameState.currentTurn}, shouldPlay=$shouldAIPlay")

        if (shouldAIPlay) {
            delay(500)
            val result = try {
                withContext(Dispatchers.Default) {
                    getNextBestMove(vmGameState, depth = vmDifficulty.aiDepth)
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                null
            }

            println("AI calculated move: ${result?.move}")

            result?.move?.let { move ->
                applyMove(move.from, move.to)
            }
        }
    }

    fun startNewGame(playerSide: Color) {
        viewModel.updatePlayerSide(playerSide)
        viewModel.updateHistory(emptyList())
        viewModel.updateMoveIndex(-1)
        viewModel.updateGameState(initialGameState())

        showNewGameDialog = false
        showGameOverDialog = false
        showAboutDialog = false

        // Reiniciar estado de IA
        stopAI = false

        scope.launch {
            drawerState.close()
        }

        println("New game started: playerSide=$playerSide")
        resetBoard = true
    }

    fun undoMove() {
        if (vmMoveIndex >= 0) {
            viewModel.decrementMoveIndex()
            val newState = if (vmMoveIndex < vmHistory.size) {
                vmHistory[vmMoveIndex].second
            } else {
                initialGameState()
            }
            viewModel.updateGameState(newState)
        }
        stopAI = true
    }

    fun redoMove() {
        if (vmMoveIndex < vmHistory.size - 1) {
            viewModel.incrementMoveIndex()
            val newState = vmHistory[vmMoveIndex].second
            viewModel.updateGameState(newState)
        }
    }

    fun moveToCurrentState() {
        if (vmHistory.isNotEmpty()) {
            viewModel.updateMoveIndex(vmHistory.size - 1)
            viewModel.updateGameState(vmHistory.last().second)
        }
    }

    // Diálogo About
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    // Diálogo de fin de juego
    if (showGameOverDialog) {
        GameOverDialog(
            gameOverMessage = gameOverMessage,
            onConfirmed = { startNewGame(vmPlayerSide) },
            onDismissed = { showGameOverDialog = false }
        )
    }

    // Diálogo de confirmación de nueva partida
    if (showNewGameDialog) {
        NewGameDialog(
            onConfirmed = { startNewGame(vmPlayerSide) },
            onDismissed = { showNewGameDialog = false },
        )
    }

    val onResetBoardCompleted = {
        resetBoard = false
        println("Board reset completed")
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Sidebar(
                modifier = Modifier.systemBarsPadding(),
                gameState = vmGameState,
                moveHistory = vmHistory.map { it.first },
                currentMoveIndex = vmMoveIndex,
                isAIEnabled = vmAIEnabled,
                difficulty = vmDifficulty,
                playerSide = vmPlayerSide,
                onSettings = { navController.navigate(SettingsScreenDest.route) },
                onNewGame = { playerSide ->
                    viewModel.updatePlayerSide(playerSide)
                    showNewGameDialog = true
                },
                onToggleAI = { viewModel.updateAIEnabled(!vmAIEnabled) },
                onDifficultyChange = { viewModel.updateDifficulty(it) },
                onUndo = ::undoMove,
                onRedo = ::redoMove,
                onMoveToCurrent = ::moveToCurrentState,
                onAboutClick = { showAboutDialog = true }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        LocalizedText(id = (R.string.tarati))
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open()
                                    else drawerState.close()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.menu))
                        }
                    }
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
                    // Detectar orientación del canvas
                    if (!isLandscapeScreen) {
                        LocalizedText(
                            id = (R.string.a_board_game_by_george_spencer_brown),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Board(
                            newGame = resetBoard,
                            onResetCompleted = onResetBoardCompleted,
                            gameState = vmGameState,
                            onMove = ::applyMove,
                            boardOrientation =
                                when {
                                    isLandscapeScreen && vmPlayerSide == BLACK -> BoardOrientation.LANDSCAPE_BLACK
                                    isLandscapeScreen && vmPlayerSide == WHITE -> BoardOrientation.LANDSCAPE_WHITE
                                    vmPlayerSide == BLACK -> BoardOrientation.PORTRAIT_BLACK
                                    else -> BoardOrientation.PORTRAIT_WHITE
                                },
                        )

                        TurnIndicator(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(60.dp)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            currentTurn = vmGameState.currentTurn,
                            size = 60.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AboutDialog(onDismiss: () -> Unit = { }) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { LocalizedText(id = (R.string.about_tarati)) },
        text = {
            Column {
                LocalizedText(
                    id = R.string.tarati_is_a_strategic_board_game_created_by_george_spencer_brown,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LocalizedText(
                    id = (R.string.game_rules),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                LocalizedText(
                    id = (R.string.players_2_white_vs_black_objective_control_the_board),
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(12.dp))

                LocalizedText(
                    id = (R.string.credits),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                LocalizedText(
                    id = (R.string.original_concept_george_spencer_brown),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss
            ) {
                LocalizedText(id = (R.string.close))
            }
        }
    )
}

@Composable
fun GameOverDialog(
    gameOverMessage: String,
    onConfirmed: () -> Unit,
    onDismissed: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = onDismissed,
        title = { LocalizedText(id = (R.string.game_over)) },
        text = { Text(gameOverMessage) },
        confirmButton = {
            Button(
                onClick = onConfirmed
            ) {
                LocalizedText(id = (R.string.new_game))
            }
        },
        dismissButton = {
            Button(
                onClick = onDismissed
            ) {
                LocalizedText(id = (R.string.continue_))
            }
        }
    )
}

@Composable
fun NewGameDialog(onConfirmed: () -> Unit, onDismissed: () -> Unit = { }) {
    AlertDialog(
        onDismissRequest = onDismissed,
        title = { LocalizedText(id = (R.string.new_game)) },
        text = { LocalizedText(id = (R.string.are_you_sure_you_want_to_start_a_new_game)) },
        confirmButton = {
            Button(
                onClick = { onConfirmed() }
            ) {
                LocalizedText(R.string.yes)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismissed
            ) {
                LocalizedText(R.string.cancel)
            }
        }
    )
}

// Función base reutilizable para los previews
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreenPreviewContent(
    darkTheme: Boolean = false,
    drawerStateValue: DrawerValue = DrawerValue.Open,
    playerSide: Color = WHITE,
    landScape: Boolean,
) {
    TaratiTheme(darkTheme = darkTheme) {
        val drawerState = rememberDrawerState(initialValue = drawerStateValue)
        val scope = rememberCoroutineScope()

        val exampleMoveHistory = listOf(
            Move("C1", "B1"),
            Move("C7", "B4"),
            Move("B1", "A1"),
            Move("B4", "A1")
        )

        // Estado del juego para el preview
        var previewGameState by remember { mutableStateOf(initialGameState()) }
        var playerSide by remember { mutableStateOf(playerSide) }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                Sidebar(
                    modifier = Modifier.systemBarsPadding(),
                    gameState = previewGameState,
                    moveHistory = exampleMoveHistory, // Índice intermedio para mostrar funcionalidad
                    currentMoveIndex = 2,
                    isAIEnabled = true,
                    difficulty = Difficulty.MEDIUM,
                    playerSide = playerSide,
                    onSettings = { },
                    onNewGame = { },
                    onToggleAI = { },
                    onDifficultyChange = { },
                    onUndo = { },
                    onRedo = { },
                    onMoveToCurrent = ::initialGameState
                ) { }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { LocalizedText(R.string.tarati) },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        if (drawerState.isClosed) drawerState.open()
                                        else drawerState.close()
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
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
                        if (!landScape) {
                            LocalizedText(
                                R.string.a_board_game_by_george_spencer_brown,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Board(
                                gameState = previewGameState,
                                onMove = { from, to -> println("Move from $from to $to") },
                                boardOrientation = BoardOrientation.LANDSCAPE_BLACK,
                            )

                            TurnIndicator(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(60.dp)
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                currentTurn = previewGameState.currentTurn,
                                size = 60.dp
                            )
                        }
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
        darkTheme = false,
        drawerStateValue = DrawerValue.Open,
        landScape = false,
    )
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun MainScreenPreview_WithDrawer_Portrait_Dark() {
    MainScreenPreviewContent(
        darkTheme = true,
        drawerStateValue = DrawerValue.Open,
        landScape = false,
    )
}

// Previews Landscape
@Preview(showBackground = true, device = "spec:width=891dp,height=411dp")
@Composable
fun MainScreenPreview_WithDrawer_Landscape() {
    MainScreenPreviewContent(
        darkTheme = false,
        drawerStateValue = DrawerValue.Open,
        landScape = true,
    )
}

@Preview(showBackground = true, device = "spec:width=891dp,height=411dp")
@Composable
fun MainScreenPreview_WithDrawer_Landscape_Dark() {
    MainScreenPreviewContent(
        darkTheme = true,
        drawerStateValue = DrawerValue.Open,
        landScape = true,
    )
}

// Previews con drawer cerrado (para ver el contenido principal)
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun MainScreenPreview_DrawerClosed_Portrait() {
    MainScreenPreviewContent(
        darkTheme = false,
        drawerStateValue = DrawerValue.Closed,
        landScape = false,
    )
}

// Preview adicional: Juego en progreso con más movimientos
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun MainScreenPreview_GameInProgress() {
    MainScreenPreviewContent(
        darkTheme = false,
        drawerStateValue = DrawerValue.Open,
        landScape = false,
    )
}

@Preview(showBackground = true, device = "spec:width=891dp,height=411dp")
@Composable
fun MainScreenPreview_DrawerClosed_Landscape() {
    MainScreenPreviewContent(
        darkTheme = false,
        drawerStateValue = DrawerValue.Closed,
        landScape = true,
    )
}