package com.agustin.tarati.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.agustin.tarati.R
import com.agustin.tarati.localization.AppLanguage
import com.agustin.tarati.localization.LocalizedText
import com.agustin.tarati.localization.localizedString
import com.agustin.tarati.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onThemeChange: (AppTheme) -> Unit = {},
    onLanguageChange: (AppLanguage) -> Unit = {},
    navController: NavController
) {
    val viewModel: SettingsViewModel = viewModel()
    val settingsState by viewModel.settingsState.collectAsState()
    val currentLanguage = settingsState.language

    Scaffold(
        topBar = {
            TopAppBar(
                title = { LocalizedText(R.string.settings) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = localizedString(R.string.back)
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Sección de Idioma
            LocalizedText(
                id = R.string.language,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Alternar entre los dos idiomas
                        val newLanguage = if (currentLanguage == AppLanguage.SPANISH)
                            AppLanguage.ENGLISH else
                            AppLanguage.SPANISH
                        onLanguageChange(newLanguage)
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    LocalizedText(
                        id = when (currentLanguage) {
                            AppLanguage.SPANISH -> R.string.spanish
                            AppLanguage.ENGLISH -> R.string.english
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                    LocalizedText(
                        id = R.string.language,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = localizedString(R.string.language)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Sección de Apariencia
            LocalizedText(
                id = R.string.appearance,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            SettingsItem(
                title = localizedString(R.string.dark_theme),
                subtitle = localizedString(R.string.dark_theme),
                trailing = {
                    Switch(
                        checked = settingsState.appTheme == AppTheme.MODE_NIGHT,
                        onCheckedChange = {
                            viewModel.toggleDarkTheme(it)
                            val appTheme = if (it) AppTheme.MODE_NIGHT else AppTheme.MODE_AUTO
                            onThemeChange.invoke(appTheme)
                        }
                    )
                }
            )
        }
    }
}

// Componente reutilizable
@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    trailing: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        trailing()
    }
}