package com.agustin.tarati.ui.screens.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.agustin.tarati.R
import com.agustin.tarati.ui.localization.AppLanguage
import com.agustin.tarati.ui.localization.LocalizedText
import com.agustin.tarati.ui.localization.localizedString
import com.agustin.tarati.ui.theme.AppTheme
import com.agustin.tarati.ui.theme.availablePalettes

interface SettingsEvents {
    fun onThemeChange(theme: AppTheme)
    fun onLanguageChange(language: AppLanguage)
    fun onLabelsVisibilityChange(visible: Boolean)
    fun onVerticesVisibilityChange(visible: Boolean)
    fun onEdgesVisibilityChange(visible: Boolean)
    fun onAnimateEffectsChange(animate: Boolean)
    fun onPaletteChange(paletteName: String)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    events: SettingsEvents,
    navController: NavController
) {
    val viewModel: SettingsViewModel = viewModel()
    val settingsState by viewModel.settingsState.collectAsState()

    val language = settingsState.language
    val theme = settingsState.appTheme
    val boardState = settingsState.boardState
    val palette = settingsState.palette
    val labelsVisibility = boardState.labelsVisibles
    val verticesVisibility = boardState.verticesVisibles
    val edgesVisibility = boardState.edgesVisibles
    val animateEffects = boardState.animateEffects

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
            LanguageOption(language, events::onLanguageChange)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            ThemeOption(theme, events::onThemeChange)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            PaletteOption(palette) { newPaletteName ->
                viewModel.setPalette(newPaletteName)
                events.onPaletteChange(newPaletteName)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            VisibilityOption(
                id = R.string.board_labels,
                visibility = labelsVisibility
            ) {
                viewModel.setLabelsVisibility(it)
                events.onLabelsVisibilityChange(it)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            VisibilityOption(
                id = R.string.board_vertices,
                visibility = verticesVisibility
            ) {
                viewModel.setVerticesVisibility(it)
                events.onVerticesVisibilityChange(it)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            VisibilityOption(
                id = R.string.board_edges,
                visibility = edgesVisibility
            ) {
                viewModel.setEdgesVisibility(it)
                events.onEdgesVisibilityChange(it)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            VisibilityOption(
                id = R.string.animate_effects,
                visibility = animateEffects
            ) {
                viewModel.setAnimateEffects(it)
                events.onAnimateEffectsChange(it)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
fun PaletteOption(
    paletteName: String,
    onPaletteSelected: (String) -> Unit = {}
) {
    LocalizedText(
        id = R.string.color_palette,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(16.dp)
    )

    SettingsItem(
        title = paletteName,
        subtitle = localizedString(R.string.select_color_palette),
        trailing = {
            EnhancedPaletteSelector(
                currentPaletteName = paletteName,
                onPaletteSelected = onPaletteSelected
            )
        }
    )
}

@Composable
fun EnhancedPaletteSelector(
    currentPaletteName: String,
    onPaletteSelected: (String) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentPaletteName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(end = 8.dp)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = localizedString(R.string.select_palette)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availablePalettes.forEach { palette ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = palette.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        onPaletteSelected(palette.name)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun LanguageOption(
    language: AppLanguage,
    onChange: (AppLanguage) -> Unit = {}
) {
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
                val newLanguage = if (language == AppLanguage.SPANISH)
                    AppLanguage.ENGLISH else
                    AppLanguage.SPANISH
                onChange(newLanguage)
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            LocalizedText(
                id = when (language) {
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
}

@Composable
fun ThemeOption(
    theme: AppTheme,
    onChange: (AppTheme) -> Unit = {},
) {
    LocalizedText(
        id = R.string.appearance,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(16.dp)
    )

    SettingsItem(
        title = localizedString(
            if (theme == AppTheme.MODE_NIGHT) R.string.dark_theme
            else R.string.light_theme
        ),
        subtitle = localizedString(
            if (theme == AppTheme.MODE_NIGHT) R.string.dark_theme
            else R.string.light_theme
        ),
        trailing = {
            Switch(
                checked = theme == AppTheme.MODE_NIGHT,
                onCheckedChange = {
                    val appTheme = if (it) AppTheme.MODE_NIGHT else AppTheme.MODE_AUTO
                    onChange(appTheme)
                }
            )
        }
    )
}

@Composable
fun VisibilityOption(
    @StringRes id: Int,
    visibility: Boolean,
    onChange: (Boolean) -> Unit = {},
) {
    LocalizedText(
        id = id,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(16.dp)
    )

    SettingsItem(
        title = localizedString(
            if (visibility) R.string.show
            else R.string.hide
        ),
        subtitle = localizedString(
            if (visibility) R.string.show
            else R.string.hide
        ),
        trailing = {
            Switch(
                checked = visibility,
                onCheckedChange = onChange
            )
        }
    )
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