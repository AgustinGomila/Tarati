package com.agustin.tarati.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
import com.agustin.tarati.ui.theme.TaratiTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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
    var difficulty by remember { mutableStateOf(Difficulty.DEFAULT) }
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
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(stringResource(R.string.about_tarati)) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.tarati_is_a_strategic_board_game_created_by_george_spencer_brown),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = stringResource(R.string.game_rules),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        text = stringResource(R.string.players_2_white_vs_black_objective_control_the_board),
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.credits),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        text = stringResource(R.string.original_concept_george_spencer_brown),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false }
                ) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

    // Diálogo de fin de juego
    if (showGameOverDialog) {
        AlertDialog(
            onDismissRequest = { showGameOverDialog = false },
            title = { Text(stringResource(R.string.game_over)) },
            text = { Text(gameOverMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        showGameOverDialog = false
                        startNewGame()
                    }
                ) {
                    Text(stringResource(R.string.new_game))
                }
            },
            dismissButton = {
                Button(
                    onClick = { showGameOverDialog = false }
                ) {
                    Text(stringResource(R.string.continue_))
                }
            }
        )
    }

    // Diálogo de confirmación de nueva partida
    if (showNewGameDialog) {
        AlertDialog(
            onDismissRequest = { showNewGameDialog = false },
            title = { Text(stringResource(R.string.new_game)) },
            text = { Text(stringResource(R.string.are_you_sure_you_want_to_start_a_new_game)) },
            confirmButton = {
                Button(
                    onClick = { startNewGame() }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showNewGameDialog = false }
                ) {
                    Text("Cancel")
                }
            }
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
                onNewGame = { showNewGameDialog = true },
                onToggleAI = { isAIEnabled = !isAIEnabled },
                onDifficultyChange = { difficulty = it },
                onUndo = { undoMove() },
                onRedo = { redoMove() },
                onMoveToCurrent = { moveToCurrentState() },
                onAboutClick = {
                    showAboutDialog = true
                    scope.launch { drawerState.close() }
                },
                modifier = Modifier.fillMaxHeight()
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(stringResource(R.string.tarati))
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
                    Text(
                        text = stringResource(R.string.a_board_game_by_george_spencer_brown),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

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

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun MainScreenPreview() {
    TaratiTheme {
        MainScreen()
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun MainScreenPreview_Dark() {
    TaratiTheme(darkTheme = true) {
        MainScreen()
    }
}

// Preview simplificado del drawer abierto
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
fun MainScreenPreview_WithDrawer() {
    TaratiTheme {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
        val scope = rememberCoroutineScope()

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                // TODO: Reutilizar el SidebarPreview aquí
                Box(
                    modifier = Modifier
                        .width(280.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Menu Lateral Abierto", style = MaterialTheme.typography.headlineSmall)
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Tarati") },
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
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Contenido Principal",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { }) {
                            Text("Abrir Drawer")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AboutDialogPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("About Tarati") },
                text = {
                    Column {
                        Text(
                            text = "Tarati is a strategic board game created by George Spencer Brown, " +
                                    "based on his work 'Laws of Form'. The game combines elements of " +
                                    "traditional board games with unique movement and capture mechanics.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Game Rules:",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Text(
                            text = "• Players: 2 (WHITE vs BLACK)\n" +
                                    "• Objective: Control the board by converting opponent pieces\n" +
                                    "• Movement: Pieces move along edges to adjacent vertices\n" +
                                    "• Upgrades: Pieces become upgraded in opponent's home base\n" +
                                    "• Capture: Moving adjacent to opponent pieces converts them",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Credits:",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Text(
                            text = "• Original concept: George Spencer Brown\n" +
                                    "• React implementation: Adam Blvck\n" +
                                    "• Kotlin/Android port: Agustín Gomila",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { }
                    ) {
                        Text("Close")
                    }
                }
            )
        }
    }
}