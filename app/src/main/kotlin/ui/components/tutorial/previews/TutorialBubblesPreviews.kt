package com.agustin.tarati.ui.components.tutorial.previews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agustin.tarati.R
import com.agustin.tarati.ui.components.tutorial.BubbleConfig
import com.agustin.tarati.ui.components.tutorial.BubblePosition
import com.agustin.tarati.ui.components.tutorial.TutorialBubble
import com.agustin.tarati.ui.components.tutorial.TutorialBubbleContentState
import com.agustin.tarati.ui.components.tutorial.TutorialBubbleEvents
import com.agustin.tarati.ui.components.tutorial.TutorialBubbleState
import com.agustin.tarati.ui.localization.localizedString
import com.agustin.tarati.ui.theme.TaratiTheme


private val previewBubbleEvents by lazy {
    object : TutorialBubbleEvents {
        override fun onNext() {}
        override fun onPrevious() {}
        override fun onSkip() {}
        override fun onRepeat() {}
    }
}

@Preview(name = "Quick - Burbuja Simple", group = "Tutorial Quick")
@Composable
fun QuickTutorialBubblePreview() {
    TaratiTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                TutorialBubble(
                    title = "Título del Paso",
                    bubbleState = TutorialBubbleState(
                        contentState = TutorialBubbleContentState(
                            description = "Descripción breve del paso actual del tutorial.",
                            canGoBack = true,
                            canGoForward = true,
                            currentStep = 3,
                            totalSteps = 11,
                        ),
                        config = BubbleConfig(BubblePosition.BOTTOM_CENTER),
                    ),
                    bubbleEvents = previewBubbleEvents,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }
    }
}

@Preview(name = "Quick - Múltiples Burbujas", group = "Tutorial Quick")
@Composable
fun QuickMultipleBubblesPreview() {
    TaratiTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Burbuja en esquina superior izquierda
            TutorialBubble(
                title = localizedString(R.string.tutorial_introduction_title),
                bubbleState = TutorialBubbleState(
                    contentState = TutorialBubbleContentState(
                        description = localizedString(R.string.tutorial_introduction_description),
                        canGoBack = false,
                        canGoForward = true,
                        currentStep = 1,
                        totalSteps = 11,
                    ),
                    config = BubbleConfig(
                        BubblePosition.TOP_LEFT,
                        width = 280.dp
                    ),
                ),
                bubbleEvents = previewBubbleEvents,
                modifier = Modifier.align(Alignment.TopStart)
            )

            // Burbuja en esquina inferior derecha
            TutorialBubble(
                title = localizedString(R.string.tutorial_completed_title),
                bubbleState = TutorialBubbleState(
                    contentState = TutorialBubbleContentState(
                        description = localizedString(R.string.tutorial_completed_description),
                        canGoBack = true,
                        canGoForward = false,
                        currentStep = 11,
                        totalSteps = 11,
                    ),
                    config = BubbleConfig(
                        BubblePosition.BOTTOM_RIGHT,
                        width = 280.dp
                    ),
                ),
                bubbleEvents = previewBubbleEvents,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

@Preview(name = "Burbuja Centro Inferior", group = "Tutorial Burbujas")
@Composable
fun TutorialBubblePreview_BottomCenter() {
    TaratiTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            TutorialBubble(
                title = localizedString(R.string.tutorial_basic_moves_title),
                bubbleState = TutorialBubbleState(
                    contentState = TutorialBubbleContentState(
                        description = localizedString(R.string.tutorial_basic_moves_description),
                        canGoBack = true,
                        canGoForward = true,
                        currentStep = 2,
                        totalSteps = 11,
                    ),
                    config = BubbleConfig(BubblePosition.BOTTOM_CENTER),
                ),
                bubbleEvents = previewBubbleEvents,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Preview(name = "Burbuja Superior Derecha", group = "Tutorial Burbujas")
@Composable
fun TutorialBubblePreview_TopRight() {
    TaratiTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            TutorialBubble(
                title = localizedString(R.string.tutorial_center_title),
                bubbleState = TutorialBubbleState(
                    contentState = TutorialBubbleContentState(
                        description = localizedString(R.string.tutorial_center_description),
                        canGoBack = true,
                        canGoForward = true,
                        currentStep = 4,
                        totalSteps = 11,
                    ),
                    config = BubbleConfig(
                        BubblePosition.TOP_RIGHT,
                        width = 280.dp
                    ),
                ),
                bubbleEvents = previewBubbleEvents,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

@Preview(name = "Burbuja Centro Izquierda", group = "Tutorial Burbujas")
@Composable
fun TutorialBubblePreview_CenterLeft() {
    TaratiTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            TutorialBubble(
                title = localizedString(R.string.tutorial_captures_title),
                bubbleState = TutorialBubbleState(
                    contentState = TutorialBubbleContentState(
                        description = localizedString(R.string.tutorial_captures_description),
                        canGoBack = true,
                        canGoForward = true,
                        currentStep = 6,
                        totalSteps = 11,
                    ),
                    config = BubbleConfig(
                        BubblePosition.CENTER_LEFT,
                        width = 300.dp
                    ),
                ),
                bubbleEvents = previewBubbleEvents,
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }
    }
}

@Preview(name = "Burbuja Sin Anterior", group = "Tutorial Burbujas")
@Composable
fun TutorialBubblePreview_NoBack() {
    TaratiTheme {
        Box(
            modifier = Modifier
                .size(400.dp, 240.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TutorialBubble(
                title = localizedString(R.string.tutorial_bridge_title),
                bubbleState = TutorialBubbleState(
                    contentState = TutorialBubbleContentState(
                        description = localizedString(R.string.tutorial_bridge_description),
                        canGoBack = true,
                        canGoForward = true,
                        currentStep = 1,
                        totalSteps = 11,
                    ),
                    config = BubbleConfig(BubblePosition.TOP_CENTER),
                ),
                bubbleEvents = previewBubbleEvents,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Preview(name = "Burbuja Último Paso", group = "Tutorial Burbujas")
@Composable
fun TutorialBubblePreview_LastStep() {
    TaratiTheme {
        Box(
            modifier = Modifier
                .size(400.dp, 240.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TutorialBubble(
                title = localizedString(R.string.tutorial_circumference_title),
                bubbleState = TutorialBubbleState(
                    contentState = TutorialBubbleContentState(
                        description = localizedString(R.string.tutorial_circumference_description),
                        canGoBack = true,
                        canGoForward = false,
                        currentStep = 6,
                        totalSteps = 11,
                    ),
                    config = BubbleConfig(BubblePosition.BOTTOM_CENTER),
                ),
                bubbleEvents = previewBubbleEvents,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Preview(name = "Burbuja Texto Largo", group = "Tutorial Burbujas")
@Composable
fun TutorialBubblePreview_LongText() {
    TaratiTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            TutorialBubble(
                title = localizedString(R.string.tutorial_castling_title),
                bubbleState = TutorialBubbleState(
                    contentState = TutorialBubbleContentState(
                        description = localizedString(R.string.tutorial_castling_description),
                        canGoBack = true,
                        canGoForward = true,
                        currentStep = 9,
                        totalSteps = 11,
                    ),
                    config = BubbleConfig(
                        BubblePosition.CENTER_RIGHT
                    )
                ),
                bubbleEvents = previewBubbleEvents,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}