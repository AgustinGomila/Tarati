package com.agustin.tarati.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.agustin.tarati.ui.navigation.ScreenDestinations.MainScreenDest
import com.agustin.tarati.ui.navigation.ScreenDestinations.SettingsScreenDest
import com.agustin.tarati.ui.navigation.ScreenDestinations.SplashScreenDest
import com.agustin.tarati.ui.screens.main.MainScreen
import com.agustin.tarati.ui.screens.settings.SettingsEvents
import com.agustin.tarati.ui.screens.settings.SettingsScreen
import com.agustin.tarati.ui.screens.splash.SplashScreen

@Composable
fun NavGraph(
    settingsEvents: SettingsEvents,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SplashScreenDest.route
    ) {
        composable(route = SplashScreenDest.route) {
            SplashScreen(navController = navController)
        }

        composable(route = MainScreenDest.route) {
            MainScreen(navController = navController)
        }

        composable(SettingsScreenDest.route) {
            SettingsScreen(
                events = settingsEvents, navController = navController
            )
        }
    }
}