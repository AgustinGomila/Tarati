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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.agustin.tarati.R
import com.agustin.tarati.game.Color
import com.agustin.tarati.game.Difficulty
import com.agustin.tarati.game.GameState
import com.agustin.tarati.game.Move
import com.agustin.tarati.game.TaratiAI
import com.agustin.tarati.game.applyMoveToBoard
import com.agustin.tarati.ui.components.Board
import com.agustin.tarati.ui.components.Sidebar
import com.agustin.tarati.ui.components.TurnIndicator
import com.agustin.tarati.ui.localization.LocalizedText
import com.agustin.tarati.ui.navigation.ScreenDestinations.SettingsScreenDest
import com.agustin.tarati.ui.screens.main.ScreenViewModel.Companion.initialGameState
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
    val viewModel: ScreenViewModel = viewModel()

    // Observamos el gameState del ViewModel. Si es null, creamos un estado inicial
    val vmGameState by viewModel.gameState.collectAsState(initialGameState())
    val vmHistory by viewModel.history.collectAsState(emptyList())
    val vmDifficulty by viewModel.difficulty.collectAsState(Difficulty.DEFAULT)
    val vmMoveIndex by viewModel.moveIndex.collectAsState(-1)
    val vmAIEnabled by viewModel.aIEnabled.collectAsState(true)
    val vmPlayerSide by viewModel.playerSide.collectAsState(Color.WHITE)

    var stopAI by remember { mutableStateOf(false) }
    var boardSize by remember { mutableStateOf(500.dp) }

    // Estados para diálogos
    var showGameOverDialog by remember { mutableStateOf(false) }
    var showNewGameDialog by remember { mutableStateOf(false) }
    var gameOverMessage by remember { mutableStateOf("") }
    var showAboutDialog by remember { mutableStateOf(false) }

    fun getBoardWidth(size: Dp): Float {
        return (((size.value) / 3f).dp).value
    }

    // ancho virtual por vértice
    val vWidth = getBoardWidth(boardSize)

    fun isGameOverLocal(state: GameState): Boolean {
        val possible = TaratiAI.getAllPossibleMoves(state)
        if (possible.isEmpty()) return true
        val colors = state.checkers.values.map { it.color }.toSet()
        return colors.size <= 1
    }

    fun applyMove(from: String, to: String) {
        stopAI = false

        // No rotar las coordenadas aquí, el Board ya maneja la conversión.
        // Las coordenadas from/to ya están en el sistema lógico correcto
        val actualFrom = from
        val actualTo = to

        println("Applying move: $actualFrom -> $actualTo")

        val newBoardState = applyMoveToBoard(vmGameState, actualFrom, actualTo)
        val nextState = newBoardState.copy(
            currentTurn = if (vmGameState.currentTurn == Color.WHITE) Color.BLACK else Color.WHITE
        )

        val newEntry = Pair(Move(actualFrom, actualTo), nextState)
        val truncated =
            if (vmMoveIndex < vmHistory.size - 1) {
                vmHistory.take(vmMoveIndex + 1)
            } else vmHistory

        val newMoveHistory = truncated + newEntry
        viewModel.updateMoveIndex(newMoveHistory.size - 1)

        viewModel.updateHistory(newMoveHistory)
        viewModel.updateGameState(nextState)

        if (isGameOverLocal(nextState)) {
            gameOverMessage = context.getString(
                R.string.game_over_wins,
                if (nextState.currentTurn == Color.WHITE) context.getString(R.string.black) else context.getString(R.string.white)
            )
            showGameOverDialog = true
        }
    }

    // IA Logic
    // En MainScreen.kt, corregir el LaunchedEffect de la IA:
    LaunchedEffect(vmGameState.currentTurn, vmAIEnabled, stopAI, vmDifficulty, vmPlayerSide) {
        if (!vmAIEnabled || stopAI) return@LaunchedEffect

        // La IA juega cuando no es el turno del humano
        val shouldAIPlay = vmGameState.currentTurn != vmPlayerSide

        println("AI Check: enabled=${true}, playerSide=$vmPlayerSide, currentTurn=${vmGameState.currentTurn}, shouldPlay=$shouldAIPlay")

        if (shouldAIPlay) {
            delay(500)
            val result = try {
                withContext(Dispatchers.Default) {
                    TaratiAI.getNextBestMove(vmGameState, depth = vmDifficulty.depth, isMaximizingPlayer = true)
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

        // Siempre empezar con BLANCAS, sin importar el lado del jugador
        // Esto es importante para la lógica del juego
        val initialTurn = Color.WHITE

        viewModel.updateGameState(initialGameState(initialTurn))
        showNewGameDialog = false

        // Reiniciar estado de IA
        stopAI = false

        scope.launch {
            drawerState.close()
        }

        println("New game started: playerSide=$playerSide, initialTurn=$initialTurn")
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
        aboutDialog(showAboutDialog = { showAboutDialog = it })
    }

    // Diálogo de fin de juego
    if (showGameOverDialog) {
        gameOverDialog(
            gameOverMessage = gameOverMessage,
            onConfirmed = {
                showGameOverDialog = false
                startNewGame(vmPlayerSide)
            },
            onDismissed = { showGameOverDialog = false }
        )
    }

    // Diálogo de confirmación de nueva partida
    if (showNewGameDialog) {
        newGameDialog(
            onConfirmed = { startNewGame(vmPlayerSide) },
            onDismissed = { showNewGameDialog = false }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Sidebar(
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
                onAboutClick = {
                    showAboutDialog = true
                    scope.launch { drawerState.close() }
                },
                modifier = Modifier.systemBarsPadding()
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
                            gameState = vmGameState,
                            boardSize = boardSize,
                            vWidth = vWidth,
                            onMove = { from, to -> applyMove(from, to) },
                            playerSide = vmPlayerSide,
                            modifier = Modifier
                                .fillMaxWidth(0.95f)
                                .padding(8.dp)
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
fun aboutDialog(showAboutDialog: (Boolean) -> Unit) {
    AlertDialog(
        onDismissRequest = { showAboutDialog(false) },
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
                onClick = { showAboutDialog(false) }
            ) {
                LocalizedText(id = (R.string.close))
            }
        }
    )
}

@Composable
fun gameOverDialog(
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
fun newGameDialog(onConfirmed: () -> Unit, onDismissed: () -> Unit = { }) {
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
    playerSide: Color = Color.WHITE,
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
        var boardSize by remember { mutableStateOf(500.dp) }
        var playerSide by remember { mutableStateOf(playerSide) }

        // Función auxiliar para calcular vWidth (igual que en MainScreen)
        fun getBoardWidth(size: Dp): Float {
            return (((size.value) / 3f).dp).value
        }

        val vWidth = getBoardWidth(boardSize)

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                Sidebar(
                    gameState = previewGameState,
                    moveHistory = exampleMoveHistory,
                    currentMoveIndex = 2, // Índice intermedio para mostrar funcionalidad
                    isAIEnabled = true,
                    difficulty = Difficulty.MEDIUM,
                    playerSide = playerSide,
                    onSettings = { },
                    onNewGame = { },
                    onToggleAI = { },
                    onDifficultyChange = { },
                    onUndo = { },
                    onRedo = { },
                    onMoveToCurrent = ::initialGameState,
                    onAboutClick = { },
                    modifier = Modifier.systemBarsPadding()
                )
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
                            // Usamos el Board real en lugar de placeholder
                            Board(
                                gameState = previewGameState,
                                boardSize = boardSize,
                                vWidth = vWidth,
                                onMove = { from, to ->
                                    // Simulación de movimiento para el preview
                                    previewGameState = initialGameState(
                                        if (previewGameState.currentTurn == Color.WHITE)
                                            Color.BLACK
                                        else
                                            Color.WHITE
                                    )
                                },
                                playerSide = playerSide,
                                modifier = Modifier
                                    .fillMaxWidth(0.95f)
                                    .padding(8.dp)
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