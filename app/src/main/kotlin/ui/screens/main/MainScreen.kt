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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.agustin.tarati.R
import com.agustin.tarati.game.Checker
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

    // Estados del juego
    fun initialGameState(): GameState {
        val map = mapOf(
            "C1" to Checker(Color.WHITE, false),
            "C2" to Checker(Color.WHITE, false),
            "D1" to Checker(Color.WHITE, false),
            "D2" to Checker(Color.WHITE, false),
            "C7" to Checker(Color.BLACK, false),
            "C8" to Checker(Color.BLACK, false),
            "D3" to Checker(Color.BLACK, false),
            "D4" to Checker(Color.BLACK, false)
        )
        return GameState(checkers = map, currentTurn = Color.WHITE)
    }

    var gameState by remember { mutableStateOf(initialGameState()) }
    var moveHistory by remember { mutableStateOf<List<Pair<Move, GameState>>>(emptyList()) }
    var currentMoveIndex by remember { mutableIntStateOf(-1) }
    var isAIEnabled by remember { mutableStateOf(true) }
    var stopAI by remember { mutableStateOf(false) }
    var difficulty by remember { mutableStateOf(Difficulty.MEDIUM) }
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
        val newBoardState = applyMoveToBoard(gameState, from, to)
        val nextState = newBoardState.copy(
            currentTurn = if (gameState.currentTurn == Color.WHITE) Color.BLACK else Color.WHITE
        )

        val newEntry = Pair(Move(from, to), nextState)
        val truncated = if (currentMoveIndex < moveHistory.size - 1) {
            moveHistory.take(currentMoveIndex + 1)
        } else moveHistory
        val newMoveHistory = truncated + newEntry
        moveHistory = newMoveHistory
        currentMoveIndex = newMoveHistory.size - 1
        gameState = nextState

        if (isGameOverLocal(nextState)) {
            gameOverMessage = context.getString(
                R.string.game_over_wins,
                if (nextState.currentTurn == Color.WHITE) context.getString(R.string.black) else context.getString(R.string.white)
            )
            showGameOverDialog = true
        }
    }

    // IA Logic
    LaunchedEffect(gameState.currentTurn, isAIEnabled, stopAI, difficulty) {
        if (!isAIEnabled || stopAI) return@LaunchedEffect

        if (gameState.currentTurn == Color.BLACK) {
            delay(1000)
            val result = try {
                withContext(Dispatchers.Default) {
                    TaratiAI.getNextBestMove(gameState, depth = difficulty.depth, isMaximizingPlayer = true)
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                null
            }

            result?.move?.let { move ->
                applyMove(move.from, move.to)
            }
        }
    }

    fun clearHistory() {
        moveHistory = emptyList()
        currentMoveIndex = -1
        gameState = initialGameState()
        showNewGameDialog = false
    }

    fun startNewGame() {
        clearHistory()

        // Cerrar el drawer
        scope.launch {
            drawerState.close()
        }
    }

    fun undoMove() {
        if (currentMoveIndex >= 0) {
            currentMoveIndex--
            gameState = if (currentMoveIndex >= 0 && currentMoveIndex < moveHistory.size) {
                moveHistory[currentMoveIndex].second
            } else {
                initialGameState()
            }
        }
        stopAI = true
    }

    fun redoMove() {
        if (currentMoveIndex < moveHistory.size - 1) {
            currentMoveIndex++
            gameState = moveHistory[currentMoveIndex].second
        }
    }

    fun moveToCurrentState() {
        if (moveHistory.isNotEmpty()) {
            currentMoveIndex = moveHistory.size - 1
            gameState = moveHistory.last().second
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
            showGameOverDialog = { showGameOverDialog = it },
            onConfirmed = ::startNewGame
        )
    }

    // Diálogo de confirmación de nueva partida
    if (showNewGameDialog) {
        newGameDialog(
            showNewGameDialog = { showNewGameDialog = it },
            onConfirmed = ::startNewGame
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Sidebar(
                gameState = gameState,
                moveHistory = moveHistory.map { it.first },
                currentMoveIndex = currentMoveIndex,
                isAIEnabled = isAIEnabled,
                difficulty = difficulty,
                onSettings = { navController.navigate(SettingsScreenDest.route) },
                onNewGame = { showNewGameDialog = true },
                onToggleAI = { isAIEnabled = !isAIEnabled },
                onDifficultyChange = { difficulty = it },
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
                            gameState = gameState,
                            boardSize = boardSize,
                            vWidth = vWidth,
                            onMove = { from, to -> applyMove(from, to) },
                            modifier = Modifier
                                .fillMaxWidth(0.95f)
                                .padding(8.dp)
                        )

                        TurnIndicator(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(60.dp)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            currentTurn = gameState.currentTurn,
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
    showGameOverDialog: (Boolean) -> Unit,
    onConfirmed: () -> Unit,
    onDismissed: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = { showGameOverDialog(false) },
        title = { LocalizedText(id = (R.string.game_over)) },
        text = { Text(gameOverMessage) },
        confirmButton = {
            Button(
                onClick = {
                    showGameOverDialog(false)
                    onConfirmed()
                }
            ) {
                LocalizedText(id = (R.string.new_game))
            }
        },
        dismissButton = {
            Button(
                onClick = { showGameOverDialog(false) }
            ) {
                LocalizedText(id = (R.string.continue_))
            }
        }
    )
}

@Composable
fun newGameDialog(showNewGameDialog: (Boolean) -> Unit, onConfirmed: () -> Unit, onDismissed: () -> Unit = { }) {
    AlertDialog(
        onDismissRequest = { showNewGameDialog(false) },
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
                onClick = { showNewGameDialog(false) }
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
    landScape: Boolean,
) {
    TaratiTheme(darkTheme = darkTheme) {
        val drawerState = rememberDrawerState(initialValue = drawerStateValue)
        val scope = rememberCoroutineScope()
        val navController = rememberNavController()

        // Reutilizamos la misma función de initialGameState del código principal
        fun initialGameState(currentTurn: Color = Color.WHITE): GameState {
            val map = mapOf(
                "C1" to Checker(Color.WHITE, false),
                "C2" to Checker(Color.WHITE, false),
                "D1" to Checker(Color.WHITE, false),
                "D2" to Checker(Color.WHITE, false),
                "C7" to Checker(Color.BLACK, false),
                "C8" to Checker(Color.BLACK, false),
                "D3" to Checker(Color.BLACK, false),
                "D4" to Checker(Color.BLACK, false)
            )
            return GameState(map, currentTurn)
        }

        val exampleMoveHistory = listOf(
            Move("C1", "B1"),
            Move("C7", "B4"),
            Move("B1", "A1"),
            Move("B4", "A1")
        )

        // Estado del juego para el preview
        var previewGameState by remember { mutableStateOf(initialGameState()) }
        var boardSize by remember { mutableStateOf(500.dp) }

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
                    onSettings = { },
                    onNewGame = { },
                    onToggleAI = { },
                    onDifficultyChange = { },
                    onUndo = { previewGameState = initialGameState(Color.BLACK) },
                    onRedo = { previewGameState = initialGameState(Color.WHITE) },
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