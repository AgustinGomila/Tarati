package com.agustin.tarati.ui.components.board.draw.previews

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agustin.tarati.game.core.GameBoard
import com.agustin.tarati.game.core.GameBoard.getCentralRegions
import com.agustin.tarati.game.core.GameBoard.getDomesticRegions
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.ui.components.board.animation.RegionHighlight
import com.agustin.tarati.ui.components.board.draw.drawBoardPatternSingleColor
import com.agustin.tarati.ui.components.board.draw.drawBoardPatternTwoColors
import com.agustin.tarati.ui.components.board.draw.drawRegionHighlight
import com.agustin.tarati.ui.theme.BoardColors
import com.agustin.tarati.ui.theme.ClassicPalette
import com.agustin.tarati.ui.theme.DarkPalette
import com.agustin.tarati.ui.theme.NaturePalette
import com.agustin.tarati.ui.theme.TaratiTheme


@Preview(group = "Regions", showBackground = true, widthDp = 400, heightDp = 200)
@Composable
fun PreviewRegionHighlightsNature(
    boardColors: BoardColors = getBoardColors(NaturePalette),
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    PreviewRegionHighlights(boardColors, boardOrientation)
}

@Preview(group = "Regions", showBackground = true, widthDp = 400, heightDp = 200)
@Composable
fun PreviewRegionHighlightsDark(
    boardColors: BoardColors = getBoardColors(DarkPalette),
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    PreviewRegionHighlights(boardColors, boardOrientation)
}

@Preview(group = "Regions", showBackground = true, widthDp = 400, heightDp = 200)
@Composable
fun PreviewRegionHighlightsClassic(
    boardColors: BoardColors = getBoardColors(ClassicPalette),
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    PreviewRegionHighlights(boardColors, boardOrientation)
}

@Composable
fun PreviewRegionHighlights(
    boardColors: BoardColors = getBoardColors(DarkPalette),
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    TaratiTheme {
        Column {
            Text(
                text = "Resaltados de Regiones",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row {
                // Región central con pulso
                Surface(
                    modifier = Modifier.size(150.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            val centralRegion = getCentralRegions().firstOrNull()
                            if (centralRegion != null) {
                                drawRegionHighlight(
                                    highlight = RegionHighlight(
                                        region = centralRegion,
                                        duration = 500L,
                                        pulse = true
                                    ),
                                    canvasSize = size,
                                    orientation = boardOrientation,
                                    colors = boardColors
                                )
                            }
                        }
                        Text(
                            text = "Central con Pulso",
                            modifier = Modifier.align(Alignment.TopCenter),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                // Región doméstica sin pulso
                Surface(
                    modifier = Modifier.size(150.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            val domesticRegion = getDomesticRegions().firstOrNull()
                            if (domesticRegion != null) {
                                drawRegionHighlight(
                                    highlight = RegionHighlight(
                                        region = domesticRegion,
                                        duration = 500L,
                                        pulse = false
                                    ),
                                    canvasSize = size,
                                    orientation = boardOrientation,
                                    colors = boardColors
                                )
                            }
                        }
                        Text(
                            text = "Doméstica sin Pulso",
                            modifier = Modifier.align(Alignment.TopCenter),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                // Región de circunferencia
                Surface(
                    modifier = Modifier.size(150.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            val circumferenceRegion = GameBoard.getCircumferenceRegions().firstOrNull()
                            if (circumferenceRegion != null) {
                                drawRegionHighlight(
                                    highlight = RegionHighlight(
                                        region = circumferenceRegion,
                                        duration = 500L,
                                        pulse = true
                                    ),
                                    canvasSize = size,
                                    orientation = boardOrientation,
                                    colors = boardColors
                                )
                            }
                        }
                        Text(
                            text = "Circunferencia",
                            modifier = Modifier.align(Alignment.TopCenter),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Preview(group = "Regions", showBackground = true, widthDp = 300, heightDp = 200)
@Composable
fun PreviewBoardPatternsNature(
    boardColors: BoardColors = getBoardColors(NaturePalette),
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    PreviewBoardPatterns(boardColors, boardOrientation)
}

@Preview(group = "Regions", showBackground = true, widthDp = 300, heightDp = 200)
@Composable
fun PreviewBoardPatternsDark(
    boardColors: BoardColors = getBoardColors(DarkPalette),
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    PreviewBoardPatterns(boardColors, boardOrientation)
}

@Preview(group = "Regions", showBackground = true, widthDp = 300, heightDp = 200)
@Composable
fun PreviewBoardPatternsClassic(
    boardColors: BoardColors = getBoardColors(ClassicPalette),
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    PreviewBoardPatterns(boardColors, boardOrientation)
}

@Composable
fun PreviewBoardPatterns(
    boardColors: BoardColors = getBoardColors(DarkPalette),
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    TaratiTheme {
        Column {
            Text(
                text = "Patrones del Tablero",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row {
                // Patrón dos colores
                Surface(
                    modifier = Modifier.size(150.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            drawBoardPatternTwoColors(
                                canvasSize = size,
                                boardRegions = getCentralRegions().take(4),
                                surfaceColor1 = boardColors.boardPatternColor3,
                                surfaceColor2 = boardColors.boardPatternColor2,
                                borderColor = boardColors.boardPatternBorderColor,
                                orientation = boardOrientation
                            )
                        }
                        Text(
                            text = "2 Colores",
                            modifier = Modifier.align(Alignment.TopCenter),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                // Patrón un color
                Surface(
                    modifier = Modifier.size(150.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            drawBoardPatternSingleColor(
                                canvasSize = size,
                                boardRegions = getDomesticRegions().take(2),
                                surfaceColor = boardColors.boardPatternColor1,
                                borderColor = boardColors.boardPatternBorderColor,
                                orientation = boardOrientation
                            )
                        }
                        Text(
                            text = "1 Color",
                            modifier = Modifier.align(Alignment.TopCenter),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}