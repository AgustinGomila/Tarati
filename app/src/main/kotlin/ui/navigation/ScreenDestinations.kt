package com.agustin.tarati.ui.navigation

sealed class ScreenDestinations(val route: String) {
    object SplashScreenDest : ScreenDestinations(route = "splash_screen")
    object MainScreenDest : ScreenDestinations(route = "main_screen")
    object SettingsScreenDest : ScreenDestinations(route = "settings_screen")
}