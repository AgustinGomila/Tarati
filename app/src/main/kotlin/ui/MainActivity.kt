package com.agustin.tarati.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.agustin.tarati.ui.localization.LanguageAwareApp
import com.agustin.tarati.ui.navigation.NavGraph
import com.agustin.tarati.ui.screens.settings.SettingsViewModel
import com.agustin.tarati.ui.theme.AppTheme
import com.agustin.tarati.ui.theme.TaratiTheme
import org.koin.core.context.GlobalContext.get
import java.util.*

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings", MODE_PRIVATE)
        val langCode = prefs.getString("language", "en") ?: "en"
        val locale = Locale.forLanguageTag(langCode)

        super.attachBaseContext(
            newBase.wrapContext(locale)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This app draws behind the system bars, so we want to handle fitting system windows
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // ViewModel que guarda estado, historial y dificultad
        val viewModel: SettingsViewModel by lazy { get().get() }

        setContent {
            LanguageAwareApp {

                val settings = viewModel.settingsState.collectAsState()
                val useDarkTheme = when (settings.value.appTheme) {
                    AppTheme.MODE_AUTO -> isSystemInDarkTheme()
                    AppTheme.MODE_DAY -> false
                    AppTheme.MODE_NIGHT -> true
                }

                TaratiTheme(darkTheme = useDarkTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavGraph(
                            onThemeChange = { viewModel.toggleDarkTheme(it == AppTheme.MODE_NIGHT) },
                            onLanguageChange = { viewModel.setLanguage(it) },
                            onLabelsVisibilityChange = { viewModel.setLabelsVisibility(it) }
                        )
                    }
                }
            }
        }
    }

    fun Context.wrapContext(locale: Locale): Context {
        val config = Configuration(resources.configuration).apply {
            setLocale(locale)
        }
        return createConfigurationContext(config)
    }
}