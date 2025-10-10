package com.agustin.tarati.ui.screens.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.agustin.tarati.game.ai.Difficulty
import com.agustin.tarati.ui.localization.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.context.GlobalContext.get

interface SettingsRepository {
    val isDarkTheme: Flow<Boolean>
    val difficulty: Flow<Difficulty>
    val language: Flow<AppLanguage>
    val labelsVisibility: Flow<Boolean>

    suspend fun setDarkTheme(enabled: Boolean)
    suspend fun setDifficulty(difficulty: Difficulty)
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setLabelsVisibility(visibility: Boolean)
}

class SettingsRepositoryImpl() : SettingsRepository {

    private var dataStore: DataStore<Preferences> = get().get()

    companion object {
        val DARK_THEME_KEY = booleanPreferencesKey("dark_theme_enabled")
        val DIFFICULTY_KEY = intPreferencesKey("difficulty")
        val LANGUAGE_KEY = stringPreferencesKey("app_language")
        val LABELS_VISIBILITY_KEY = booleanPreferencesKey("labels_visibles")
    }

    override val isDarkTheme: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[DARK_THEME_KEY] == true }

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

    override val labelsVisibility: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[LABELS_VISIBILITY_KEY] == true }

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

    override suspend fun setLabelsVisibility(visibility: Boolean) {
        dataStore.edit { settings ->
            settings[LABELS_VISIBILITY_KEY] = visibility
        }
    }
}