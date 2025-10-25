package com.agustin.tarati.ui.screens.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.agustin.tarati.R
import com.agustin.tarati.ui.localization.AppLanguage
import com.agustin.tarati.ui.localization.localizedString
import com.agustin.tarati.ui.theme.AppTheme
import com.agustin.tarati.ui.theme.TaratiTheme
import com.agustin.tarati.ui.theme.availablePalettes

interface SettingsEvents {
    fun onThemeChange(theme: AppTheme)
    fun onLanguageChange(language: AppLanguage)
    fun onLabelsVisibilityChange(visible: Boolean)
    fun onVerticesVisibilityChange(visible: Boolean)
    fun onEdgesVisibilityChange(visible: Boolean)
    fun onRegionsVisibilityChange(visible: Boolean)
    fun onPerimeterVisibilityChange(visible: Boolean)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = localizedString(R.string.settings),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = localizedString(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                SettingsCategory(title = R.string.general)
                LanguageSetting(
                    language = settingsState.language,
                    onLanguageChange = events::onLanguageChange
                )

                SettingsCategory(title = R.string.appearance)
                ThemeSetting(
                    theme = settingsState.appTheme,
                    onThemeChange = events::onThemeChange
                )
                PaletteSetting(
                    paletteName = settingsState.palette,
                    onPaletteSelected = { palette ->
                        viewModel.setPalette(palette)
                        events.onPaletteChange(palette)
                    }
                )

                SettingsCategory(title = R.string.board_display)
                ToggleSetting(
                    icon = Icons.Default.Visibility,
                    title = R.string.board_labels,
                    checked = settingsState.boardVisualState.labelsVisibles,
                    onCheckedChange = { visible ->
                        viewModel.setLabelsVisibility(visible)
                        events.onLabelsVisibilityChange(visible)
                    }
                )
                ToggleSetting(
                    icon = Icons.Default.Visibility,
                    title = R.string.board_vertices,
                    checked = settingsState.boardVisualState.verticesVisibles,
                    onCheckedChange = { visible ->
                        viewModel.setVerticesVisibility(visible)
                        events.onVerticesVisibilityChange(visible)
                    }
                )
                ToggleSetting(
                    icon = Icons.Default.Visibility,
                    title = R.string.board_edges,
                    checked = settingsState.boardVisualState.edgesVisibles,
                    onCheckedChange = { visible ->
                        viewModel.setEdgesVisibility(visible)
                        events.onEdgesVisibilityChange(visible)
                    }
                )
                ToggleSetting(
                    icon = Icons.Default.Visibility,
                    title = R.string.board_regions,
                    checked = settingsState.boardVisualState.regionsVisibles,
                    onCheckedChange = { visible ->
                        viewModel.setRegionsVisibility(visible)
                        events.onRegionsVisibilityChange(visible)
                    }
                )
                ToggleSetting(
                    icon = Icons.Default.Visibility,
                    title = R.string.board_perimeter,
                    checked = settingsState.boardVisualState.perimeterVisible,
                    onCheckedChange = { visible ->
                        viewModel.setPerimeterVisibility(visible)
                        events.onPerimeterVisibilityChange(visible)
                    }
                )

                SettingsCategory(title = R.string.animations)
                ToggleSetting(
                    icon = Icons.Default.Animation,
                    title = R.string.animate_effects,
                    checked = settingsState.boardVisualState.animateEffects,
                    onCheckedChange = { animate ->
                        viewModel.setAnimateEffects(animate)
                        events.onAnimateEffectsChange(animate)
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsCategory(@StringRes title: Int) {
    Text(
        text = localizedString(title),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
private fun LanguageSetting(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    SettingItem(
        icon = Icons.Default.Language,
        title = when (language) {
            AppLanguage.SPANISH -> R.string.spanish
            AppLanguage.ENGLISH -> R.string.english
        },
        subtitle = R.string.language
    ) {
        Box {
            Row(
                modifier = Modifier
                    .clickable { expanded = true }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = localizedString(
                        when (language) {
                            AppLanguage.SPANISH -> R.string.spanish
                            AppLanguage.ENGLISH -> R.string.english
                        }
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(localizedString(R.string.spanish)) },
                    onClick = {
                        onLanguageChange(AppLanguage.SPANISH)
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(localizedString(R.string.english)) },
                    onClick = {
                        onLanguageChange(AppLanguage.ENGLISH)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ThemeSetting(
    theme: AppTheme,
    onThemeChange: (AppTheme) -> Unit
) {
    SettingItem(
        icon = Icons.Default.DarkMode,
        title = if (theme == AppTheme.MODE_NIGHT) R.string.dark_theme else R.string.light_theme,
        subtitle = R.string.appearance
    ) {
        Switch(
            checked = theme == AppTheme.MODE_NIGHT,
            onCheckedChange = {
                val newTheme = if (it) AppTheme.MODE_NIGHT else AppTheme.MODE_AUTO
                onThemeChange(newTheme)
            }
        )
    }
}

@Composable
private fun PaletteSetting(
    paletteName: String,
    onPaletteSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    SettingItem(
        icon = Icons.Default.Palette,
        title = R.string.color_palette,
        subtitle = R.string.select_color_palette
    ) {
        Box {
            Row(
                modifier = Modifier
                    .clickable { expanded = true }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = paletteName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
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
}

@Composable
private fun ToggleSetting(
    icon: ImageVector,
    @StringRes title: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SettingItem(
        icon = icon,
        title = title,
        subtitle = if (checked) R.string.enabled else R.string.disabled
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    @StringRes title: Int,
    @StringRes subtitle: Int,
    trailingContent: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 16.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = localizedString(title),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = localizedString(subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        trailingContent()
    }

    HorizontalDivider(
        modifier = Modifier.padding(start = 56.dp, end = 16.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
    )
}

// region Previews

@Preview(name = "Language Setting")
@Composable
fun LanguageSettingPreview() {
    TaratiTheme {
        Column {
            LanguageSetting(
                language = AppLanguage.SPANISH,
                onLanguageChange = {}
            )
            LanguageSetting(
                language = AppLanguage.ENGLISH,
                onLanguageChange = {}
            )
        }
    }
}

@Preview(name = "Theme Setting")
@Composable
fun ThemeSettingPreview() {
    TaratiTheme {
        Column {
            ThemeSetting(
                theme = AppTheme.MODE_AUTO,
                onThemeChange = {}
            )
            ThemeSetting(
                theme = AppTheme.MODE_NIGHT,
                onThemeChange = {}
            )
        }
    }
}

@Preview(name = "Palette Setting")
@Composable
fun PaletteSettingPreview() {
    TaratiTheme {
        PaletteSetting(
            paletteName = "Default Palette",
            onPaletteSelected = {}
        )
    }
}

@Preview(name = "Toggle Settings")
@Composable
fun ToggleSettingsPreview() {
    TaratiTheme {
        Column {
            ToggleSetting(
                icon = Icons.Default.Visibility,
                title = R.string.board_labels,
                checked = true,
                onCheckedChange = {}
            )
            ToggleSetting(
                icon = Icons.Default.Visibility,
                title = R.string.board_vertices,
                checked = false,
                onCheckedChange = {}
            )
            ToggleSetting(
                icon = Icons.Default.Animation,
                title = R.string.animate_effects,
                checked = true,
                onCheckedChange = {}
            )
            ToggleSetting(
                icon = Icons.Default.Visibility,
                title = R.string.board_regions,
                checked = false,
                onCheckedChange = {}
            )
            ToggleSetting(
                icon = Icons.Default.Visibility,
                title = R.string.board_perimeter,
                checked = false,
                onCheckedChange = {}
            )
        }
    }
}

@Preview(name = "Setting Item")
@Composable
fun SettingItemPreview() {
    TaratiTheme {
        Column {
            SettingItem(
                icon = Icons.Default.Language,
                title = R.string.language,
                subtitle = R.string.language,
                trailingContent = {
                    Text("ES", color = MaterialTheme.colorScheme.primary)
                }
            )
            SettingItem(
                icon = Icons.Default.DarkMode,
                title = R.string.dark_theme,
                subtitle = R.string.dark_theme,
                trailingContent = {
                    Switch(checked = true, onCheckedChange = {})
                }
            )
        }
    }
}

@Preview(name = "Settings Categories")
@Composable
fun SettingsCategoriesPreview() {
    TaratiTheme {
        Column {
            SettingsCategory(title = R.string.general)
            SettingsCategory(title = R.string.appearance)
            SettingsCategory(title = R.string.board_display)
            SettingsCategory(title = R.string.animations)
        }
    }
}

// endregion Previews