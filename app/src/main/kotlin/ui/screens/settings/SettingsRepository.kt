package com.agustin.tarati.ui.screens.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.agustin.tarati.game.ai.Difficulty
import com.agustin.tarati.ui.localization.AppLanguage
import com.agustin.tarati.ui.theme.availablePalettes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SettingsRepository {
    val isDarkTheme: Flow<Boolean>
    val difficulty: Flow<Difficulty>
    val language: Flow<AppLanguage>
    val palette: Flow<String>
    val labelsVisibility: Flow<Boolean>
    val tutorialButtonVisibility: Flow<Boolean>
    val verticesVisibility: Flow<Boolean>
    val edgesVisibility: Flow<Boolean>
    val animateEffects: Flow<Boolean>

    suspend fun setDarkTheme(enabled: Boolean)
    suspend fun setDifficulty(difficulty: Difficulty)
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setPalette(paletteName: String)
    suspend fun setLabelsVisibility(visibility: Boolean)
    suspend fun setTutorialButtonVisibility(visibility: Boolean)
    suspend fun setVerticesVisibility(visibility: Boolean)
    suspend fun setEdgesVisibility(visibility: Boolean)
    suspend fun setAnimateEffects(animate: Boolean)
}

class SettingsRepositoryImpl(var dataStore: DataStore<Preferences>) : SettingsRepository {

    companion object {
        val DARK_THEME_KEY = booleanPreferencesKey("dark_theme_enabled")
        val DIFFICULTY_KEY = intPreferencesKey("difficulty")
        val LANGUAGE_KEY = stringPreferencesKey("app_language")
        val PALETTE_KEY = stringPreferencesKey("app_palette")
        val LABELS_VISIBILITY_KEY = booleanPreferencesKey("labels_visibles")
        val TUTORIAL_BUTTON_VISIBILITY_KEY = booleanPreferencesKey("tutorial_button_visible")
        val VERTICES_VISIBILITY_KEY = booleanPreferencesKey("vertices_visibles")
        val EDGES_VISIBILITY_KEY = booleanPreferencesKey("edges_visibles")
        val ANIMATE_EFFECTS_KEY = booleanPreferencesKey("animate_effects")

        private const val LABELS_VISIBILITY_DEFAULT = false
        private const val TUTORIAL_BUTTON_VISIBILITY_DEFAULT = true
        private const val VERTICES_VISIBILITY_DEFAULT = true
        private const val EDGES_VISIBILITY_DEFAULT = false
        private const val ANIMATE_EFFECTS_DEFAULT = true

    }

    override val isDarkTheme: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[DARK_THEME_KEY]?.let { true } ?: false }

    override val difficulty: Flow<Difficulty> = dataStore.data
        .map { preferences ->
            preferences[DIFFICULTY_KEY]?.let { depth ->
                Difficulty.getByDepth(depth)
            } ?: Difficulty.DEFAULT
        }

    override val language: Flow<AppLanguage> = dataStore.data
        .map { preferences ->
            preferences[LANGUAGE_KEY]?.let { langName ->
                AppLanguage.valueOf(langName)
            } ?: AppLanguage.SPANISH
        }

    override val palette: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PALETTE_KEY] ?: availablePalettes.first().name
        }

    override val labelsVisibility: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[LABELS_VISIBILITY_KEY] ?: LABELS_VISIBILITY_DEFAULT }

    override val tutorialButtonVisibility: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[TUTORIAL_BUTTON_VISIBILITY_KEY] ?: TUTORIAL_BUTTON_VISIBILITY_DEFAULT }

    override val verticesVisibility: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[VERTICES_VISIBILITY_KEY] ?: VERTICES_VISIBILITY_DEFAULT }

    override val edgesVisibility: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[EDGES_VISIBILITY_KEY] ?: EDGES_VISIBILITY_DEFAULT }

    override val animateEffects: Flow<Boolean> =
        dataStore.data.map { preferences -> preferences[ANIMATE_EFFECTS_KEY] ?: ANIMATE_EFFECTS_DEFAULT }


    override suspend fun setDarkTheme(enabled: Boolean) {
        dataStore.edit { settings ->
            settings[DARK_THEME_KEY] = enabled
        }
    }

    override suspend fun setDifficulty(difficulty: Difficulty) {
        dataStore.edit { settings ->
            settings[DIFFICULTY_KEY] = difficulty.depth
        }
    }

    override suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { settings ->
            settings[LANGUAGE_KEY] = language.name
        }
    }

    override suspend fun setPalette(paletteName: String) {
        dataStore.edit { settings ->
            settings[PALETTE_KEY] = paletteName
        }
    }

    override suspend fun setLabelsVisibility(visibility: Boolean) {
        dataStore.edit { settings ->
            settings[LABELS_VISIBILITY_KEY] = visibility
        }
    }

    override suspend fun setAnimateEffects(animate: Boolean) {
        dataStore.edit { settings ->
            settings[ANIMATE_EFFECTS_KEY] = animate
        }
    }

    override suspend fun setTutorialButtonVisibility(visibility: Boolean) {
        dataStore.edit { settings ->
            settings[TUTORIAL_BUTTON_VISIBILITY_KEY] = visibility
        }
    }

    override suspend fun setVerticesVisibility(visibility: Boolean) {
        dataStore.edit { settings ->
            settings[VERTICES_VISIBILITY_KEY] = visibility
        }
    }

    override suspend fun setEdgesVisibility(visibility: Boolean) {
        dataStore.edit { settings ->
            settings[EDGES_VISIBILITY_KEY] = visibility
        }
    }
}