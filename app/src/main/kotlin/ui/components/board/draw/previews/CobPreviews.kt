package com.agustin.tarati.ui.components.board.draw.previews

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agustin.tarati.game.core.Cob
import com.agustin.tarati.game.core.CobColor
import com.agustin.tarati.game.core.opponent
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.ui.components.board.animation.AnimatedCob
import com.agustin.tarati.ui.components.board.draw.AnimatedPieceComposable
import com.agustin.tarati.ui.components.board.draw.StaticPieceComposable
import com.agustin.tarati.ui.components.board.draw.drawAnimatedCob
import com.agustin.tarati.ui.components.board.draw.drawCob
import com.agustin.tarati.ui.theme.BoardColors
import com.agustin.tarati.ui.theme.ClassicPalette
import com.agustin.tarati.ui.theme.DarkPalette
import com.agustin.tarati.ui.theme.NaturePalette
import com.agustin.tarati.ui.theme.TaratiTheme


@Preview(group = "Cob", showBackground = true, widthDp = 500, heightDp = 200)
@Composable
fun PreviewCobDrawingNature(
    boardColors: BoardColors = getBoardColors(NaturePalette),
    color: CobColor = CobColor.WHITE
) {
    PreviewCobDrawingClassic(boardColors, color)
}

@Preview(group = "Cob", showBackground = true, widthDp = 500, heightDp = 200)
@Composable
fun PreviewCobDrawingDark(boardColors: BoardColors = getBoardColors(DarkPalette), color: CobColor = CobColor.WHITE) {
    PreviewCobDrawingClassic(boardColors, color)
}

@Preview(group = "Cob", showBackground = true, widthDp = 500, heightDp = 200)
@Composable
fun PreviewCobDrawingClassic(
    boardColors: BoardColors = getBoardColors(ClassicPalette),
    color: CobColor = CobColor.WHITE
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        PreviewCobDrawing(boardColors, color)
        PreviewCobDrawing(boardColors, color.opponent())
    }
}

@Composable
fun PreviewCobDrawing(boardColors: BoardColors = getBoardColors(ClassicPalette), color: CobColor = CobColor.WHITE) {
    TaratiTheme {
        Box(
            modifier = Modifier
                .size(240.dp)
                .background(Color.LightGray)
                .padding(48.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCob(
                    selectedVertexId = null,
                    vertexId = "A1",
                    cob = Cob(color, false),
                    colors = boardColors,
                    sizeFactor = 1.2f
                )
            }
        }
    }
}

@Preview(group = "Cob", showBackground = true, widthDp = 500, heightDp = 200)
@Composable
fun PreviewSelectedCobNature(
    boardColors: BoardColors = getBoardColors(NaturePalette),
    color: CobColor = CobColor.WHITE
) {
    PreviewSelectedCobClassic(boardColors, color)
}

@Preview(group = "Cob", showBackground = true, widthDp = 500, heightDp = 200)
@Composable
fun PreviewSelectedCobDark(boardColors: BoardColors = getBoardColors(DarkPalette), color: CobColor = CobColor.WHITE) {
    PreviewSelectedCobClassic(boardColors, color)
}

@Preview(group = "Cob", showBackground = true, widthDp = 500, heightDp = 200)
@Composable
fun PreviewSelectedCobClassic(
    boardColors: BoardColors = getBoardColors(ClassicPalette),
    color: CobColor = CobColor.WHITE
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        PreviewSelectedCobDrawing(boardColors, color)
        PreviewSelectedCobDrawing(boardColors, color.opponent())
    }
}

@Composable
fun PreviewSelectedCobDrawing(
    boardColors: BoardColors = getBoardColors(ClassicPalette),
    color: CobColor = CobColor.WHITE
) {
    TaratiTheme {
        Box(
            modifier = Modifier
                .size(240.dp)
                .background(Color.LightGray)
                .padding(48.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCob(
                    selectedVertexId = "A1",
                    vertexId = "A1",
                    cob = Cob(color, true),
                    colors = boardColors,
                    sizeFactor = 1.2f
                )
            }
        }
    }
}

@Preview(group = "Cob", showBackground = true, widthDp = 500, heightDp = 200)
@Composable
fun PreviewAnimatedCobDrawingNature(
    boardColors: BoardColors = getBoardColors(NaturePalette),
    color: CobColor = CobColor.WHITE
) {
    PreviewAnimatedCobDrawingClassic(boardColors, color)
}

@Preview(group = "Cob", showBackground = true, widthDp = 500, heightDp = 200)
@Composable
fun PreviewAnimatedCobDrawingDark(
    boardColors: BoardColors = getBoardColors(DarkPalette),
    color: CobColor = CobColor.WHITE
) {
    PreviewAnimatedCobDrawingClassic(boardColors, color)
}

@Preview(group = "Cob", showBackground = true, widthDp = 500, heightDp = 200)
@Composable
fun PreviewAnimatedCobDrawingClassic(
    boardColors: BoardColors = getBoardColors(ClassicPalette),
    color: CobColor = CobColor.WHITE
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        PreviewAnimatedCobDrawing(boardColors, color)
        PreviewAnimatedCobDrawing(boardColors, color.opponent())
    }
}

@Composable
fun PreviewAnimatedCobDrawing(
    boardColors: BoardColors = getBoardColors(ClassicPalette),
    color: CobColor = CobColor.WHITE
) {
    TaratiTheme {
        Box(
            modifier = Modifier
                .size(240.dp)
                .background(Color.LightGray)
                .padding(48.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawAnimatedCob(
                    selectedVertexId = null,
                    vertexId = "A1",
                    animatedCob = AnimatedCob(
                        vertexId = "A1",
                        cob = Cob(color, true),
                        currentPos = "A1",
                        targetPos = "A1",
                        animationProgress = 0.5f,
                        upgradeProgress = 0.7f,
                        conversionProgress = 0.0f,
                        isConverting = false
                    ),
                    colors = boardColors,
                    sizeFactor = 1.2f
                )
            }
        }
    }
}


@Preview(group = "Cob", showBackground = true, widthDp = 500, heightDp = 200)
@Composable
fun PreviewConvertingCobDrawingNature(
    boardColors: BoardColors = getBoardColors(NaturePalette),
    color: CobColor = CobColor.WHITE
) {
    PreviewConvertingCobDrawingClassic(boardColors, color)
}

@Preview(group = "Cob", showBackground = true, widthDp = 500, heightDp = 200)
@Composable
fun PreviewConvertingCobDrawingDark(
    boardColors: BoardColors = getBoardColors(DarkPalette),
    color: CobColor = CobColor.WHITE
) {
    PreviewConvertingCobDrawingClassic(boardColors, color)
}

@Preview(group = "Cob", showBackground = true, widthDp = 500, heightDp = 200)
@Composable
fun PreviewConvertingCobDrawingClassic(
    boardColors: BoardColors = getBoardColors(ClassicPalette),
    color: CobColor = CobColor.WHITE
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        PreviewConvertingCobDrawing(boardColors, color)
        PreviewConvertingCobDrawing(boardColors, color.opponent())
    }
}

@Composable
fun PreviewConvertingCobDrawing(
    boardColors: BoardColors = getBoardColors(ClassicPalette),
    color: CobColor = CobColor.WHITE
) {
    TaratiTheme {
        Box(
            modifier = Modifier
                .size(240.dp)
                .background(Color.LightGray)
                .padding(48.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawAnimatedCob(
                    selectedVertexId = null,
                    vertexId = "A1",
                    animatedCob = AnimatedCob(
                        vertexId = "A1",
                        cob = Cob(color, false),
                        currentPos = "A1",
                        targetPos = "A1",
                        animationProgress = 1.0f,
                        upgradeProgress = 0.0f,
                        conversionProgress = 0.6f,
                        isConverting = true
                    ),
                    colors = boardColors,
                    sizeFactor = 1.2f
                )
            }
        }
    }
}

@Preview(group = "Cob", showBackground = true)
@Composable
fun PreviewAnimatedCobVariantsNature(
    boardColors: BoardColors = getBoardColors(NaturePalette),
    color: CobColor = CobColor.WHITE
) {
    PreviewAnimatedCobVariants(boardColors, color)
}

@Preview(group = "Cob", showBackground = true)
@Composable
fun PreviewAnimatedCobVariantsDark(
    boardColors: BoardColors = getBoardColors(DarkPalette),
    color: CobColor = CobColor.WHITE
) {
    PreviewAnimatedCobVariants(boardColors, color)
}

@Preview(group = "Cob", showBackground = true)
@Composable
fun PreviewAnimatedCobVariants(
    boardColors: BoardColors = getBoardColors(ClassicPalette),
    color: CobColor = CobColor.WHITE
) {
    TaratiTheme {
        Column {
            Text(
                text = "Piezas Animadas",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Upgrading
            Row {
                Text(
                    text = "Upgrade en Progreso:",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                Surface(
                    modifier = Modifier.size(80.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        drawAnimatedCob(
                            selectedVertexId = null,
                            vertexId = "A1",
                            animatedCob = AnimatedCob(
                                vertexId = "A1",
                                cob = Cob(color, true),
                                currentPos = "A1",
                                targetPos = "A1",
                                animationProgress = 1.0f,
                                upgradeProgress = 0.5f,
                                conversionProgress = 0.0f,
                                isConverting = false
                            ),
                            colors = boardColors
                        )
                    }
                }

                Surface(
                    modifier = Modifier.size(80.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        drawAnimatedCob(
                            selectedVertexId = null,
                            vertexId = "A1",
                            animatedCob = AnimatedCob(
                                vertexId = "A1",
                                cob = Cob(color, true),
                                currentPos = "A1",
                                targetPos = "A1",
                                animationProgress = 1.0f,
                                upgradeProgress = 0.8f,
                                conversionProgress = 0.0f,
                                isConverting = false
                            ),
                            colors = boardColors
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Converting
            Row {
                Text(
                    text = "Conversión en Progreso:",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                Surface(
                    modifier = Modifier.size(80.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        drawAnimatedCob(
                            selectedVertexId = null,
                            vertexId = "A1",
                            animatedCob = AnimatedCob(
                                vertexId = "A1",
                                cob = Cob(color, false),
                                currentPos = "A1",
                                targetPos = "A1",
                                animationProgress = 1.0f,
                                upgradeProgress = 0.0f,
                                conversionProgress = 0.3f,
                                isConverting = true
                            ),
                            colors = boardColors
                        )
                    }
                }

                Surface(
                    modifier = Modifier.size(80.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        drawAnimatedCob(
                            selectedVertexId = null,
                            vertexId = "A1",
                            animatedCob = AnimatedCob(
                                vertexId = "A1",
                                cob = Cob(color, false),
                                currentPos = "A1",
                                targetPos = "A1",
                                animationProgress = 1.0f,
                                upgradeProgress = 0.0f,
                                conversionProgress = 0.7f,
                                isConverting = true
                            ),
                            colors = boardColors
                        )
                    }
                }
            }
        }
    }
}

@Preview(group = "Cob", showBackground = true, widthDp = 500, heightDp = 200)
@Composable
fun PreviewCobSizeVariants(
    boardColors: BoardColors = getBoardColors(ClassicPalette),
    color: CobColor = CobColor.WHITE
) {
    TaratiTheme {
        Column {
            Text(
                text = "Tamaños de Piezas",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row {
                listOf(0.8f, 1.0f, 1.2f, 1.5f).forEach { sizeFactor ->
                    Surface(
                        modifier = Modifier.size(120.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .matchParentSize()
                                    .padding(24.dp)
                            ) {
                                drawCob(
                                    selectedVertexId = null,
                                    vertexId = "A1",
                                    cob = Cob(color, true),
                                    colors = boardColors,
                                    sizeFactor = sizeFactor
                                )
                            }
                            Text(
                                text = "${sizeFactor}x",
                                modifier = Modifier.align(Alignment.TopCenter),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview(group = "Cob", showBackground = true)
@Composable
fun PreviewStaticPieceComposable(
    boardColors: BoardColors = getBoardColors(ClassicPalette),
    color: CobColor = CobColor.WHITE,
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    TaratiTheme {
        Box(modifier = Modifier.size(200.dp)) {
            StaticPieceComposable(
                vertexId = "A1",
                cob = Cob(color, true),
                containerWidth = 200,
                containerHeight = 200,
                orientation = boardOrientation,
                selectedPiece = "A1",
                colors = boardColors,
            )
        }
    }
}

@Preview(group = "Cob", showBackground = true)
@Composable
fun PreviewAnimatedPieceComposable(
    boardColors: BoardColors = getBoardColors(NaturePalette),
    color: CobColor = CobColor.WHITE,
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    TaratiTheme {
        Box(modifier = Modifier.size(200.dp)) {
            AnimatedPieceComposable(
                animatedCob = AnimatedCob(
                    vertexId = "A1",
                    cob = Cob(color, false),
                    currentPos = "A1",
                    targetPos = "B1",
                    animationProgress = 0.5f,
                    upgradeProgress = 0.0f,
                    conversionProgress = 0.0f,
                    isConverting = false
                ),
                containerWidth = 200,
                containerHeight = 200,
                orientation = boardOrientation,
                selectedPiece = null,
                colors = boardColors,
            )
        }
    }
}

@Preview(group = "Cob", showBackground = true)
@Composable
fun PreviewAnimatedPieceUpgrading(
    boardColors: BoardColors = getBoardColors(DarkPalette),
    color: CobColor = CobColor.WHITE,
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    TaratiTheme {
        Box(modifier = Modifier.size(200.dp)) {
            AnimatedPieceComposable(
                animatedCob = AnimatedCob(
                    vertexId = "A1",
                    cob = Cob(color, true),
                    currentPos = "A1",
                    targetPos = "B1",
                    animationProgress = 1.0f,
                    upgradeProgress = 0.7f,
                    conversionProgress = 0.0f,
                    isConverting = false
                ),
                containerWidth = 200,
                containerHeight = 200,
                orientation = boardOrientation,
                selectedPiece = "A1",
                colors = boardColors,
            )
        }
    }
}