package com.agustin.tarati.ui.screens.settings.previews

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.agustin.tarati.R
import com.agustin.tarati.ui.localization.AppLanguage
import com.agustin.tarati.ui.screens.settings.LanguageSetting
import com.agustin.tarati.ui.screens.settings.PaletteSetting
import com.agustin.tarati.ui.screens.settings.SettingItem
import com.agustin.tarati.ui.screens.settings.SettingsCategory
import com.agustin.tarati.ui.screens.settings.ThemeSetting
import com.agustin.tarati.ui.screens.settings.ToggleSetting
import com.agustin.tarati.ui.theme.AppTheme
import com.agustin.tarati.ui.theme.TaratiTheme


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