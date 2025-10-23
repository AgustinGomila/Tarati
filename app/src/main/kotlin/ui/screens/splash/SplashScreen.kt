package com.agustin.tarati.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.agustin.tarati.R
import com.agustin.tarati.ui.navigation.ScreenDestinations
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController? = null) {
    val rotation: Animatable<Float, AnimationVector1D> = remember { Animatable(initialValue = 0f) }

    SplashRotateAnimation(
        rotation = rotation,
        navController = navController,
        durationMillisAnimation = 2800,
        delayScreen = 10
    )

    DrawRotatedLogo(
        rotation = rotation
    )
}

@Composable
fun SplashRotateAnimation(
    rotation: Animatable<Float, AnimationVector1D>,
    navController: NavController? = null,
    durationMillisAnimation: Int,
    iterations: Int = 1,
    delayScreen: Long,
) {
    LaunchedEffect(true) {
        rotation.animateTo(
            targetValue = 360f,
            animationSpec = repeatable(
                iterations = iterations,
                animation = tween(
                    durationMillis = durationMillisAnimation,
                    delayMillis = delayScreen.toInt(),
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Restart
            )
        )
        rotation.snapTo(0f)

        delay(timeMillis = delayScreen)

        // Navegar sin parámetro (usará el valor por defecto)
        navController?.navigate(
            route = ScreenDestinations.MainScreenDest.route
        ) {
            popUpTo(route = ScreenDestinations.SplashScreenDest.route) {
                inclusive = true
            }
        }
    }
}

@Composable
fun DrawRotatedLogo(rotation: Animatable<Float, AnimationVector1D>) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = rotation.value
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(100.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewSplashScreen() {
    SplashScreen()
}