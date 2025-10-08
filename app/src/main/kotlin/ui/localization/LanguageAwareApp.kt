package com.agustin.tarati.ui.localization

import android.content.res.Configuration
import android.view.ContextThemeWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agustin.tarati.R
import com.agustin.tarati.ui.screens.settings.SettingsViewModel
import java.util.*

val LocalAppLanguage = compositionLocalOf { Locale.getDefault() }

@Composable
fun LanguageAwareApp(
    viewModel: SettingsViewModel = viewModel(),
    content: @Composable () -> Unit
) {
    val settingsState by viewModel.settingsState.collectAsState()
    val context = LocalContext.current
    val deviceConfig = LocalConfiguration.current // Configuración observable del dispositivo

    // Determinar el locale actual
    val currentLocale = remember(settingsState.language) {
        when (settingsState.language) {
            AppLanguage.SPANISH -> Locale("es")
            AppLanguage.ENGLISH -> Locale.ENGLISH
        }
    }

    // Crear contexto con configuración localizada
    val localizedContext = remember(context, currentLocale, deviceConfig) {
        // Combinar configuración del dispositivo con nuestro locale
        val mergedConfig = Configuration(deviceConfig).apply {
            setLocale(currentLocale)
        }
        context.createConfigurationContext(mergedConfig)
    }

    // Actualizar configuración de la aplicación
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, currentLocale) {
        val appContext = context.applicationContext
        val resources = appContext.resources
        val config = Configuration(deviceConfig).apply { // Usar deviceConfig aquí también
            setLocale(currentLocale)
        }

        val contextThemeWrapper = ContextThemeWrapper(appContext, R.style.Theme_Tarati)
        contextThemeWrapper.applyOverrideConfiguration(config)

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_CREATE) {
                resources.updateConfiguration(config, resources.displayMetrics)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Proveer el contexto localizado y el locale actual
    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalAppLanguage provides currentLocale
    ) {
        content()
    }
}