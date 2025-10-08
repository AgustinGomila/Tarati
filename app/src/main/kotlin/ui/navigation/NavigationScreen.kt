package com.agustin.tarati.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.agustin.tarati.localization.AppLanguage
import com.agustin.tarati.ui.screens.main.MainScreen
import com.agustin.tarati.ui.screens.settings.SettingsScreen
import com.agustin.tarati.ui.screens.splash.SplashScreen
import com.agustin.tarati.ui.theme.AppTheme

@Composable
fun NavigationScreen(
    onThemeChange: (AppTheme) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ScreenDestinations.SplashScreenDest.route
    ) {
        composable(route = ScreenDestinations.SplashScreenDest.route) {
            SplashScreen(navController = navController)
        }

        composable(route = ScreenDestinations.MainScreenDest.route) {
            MainScreen(navController = navController)
        }

        composable(ScreenDestinations.SettingsScreenDest.route) {
            SettingsScreen(
                onThemeChange = onThemeChange,
                onLanguageChange = onLanguageChange,
                navController = navController
            )
        }
    }
}