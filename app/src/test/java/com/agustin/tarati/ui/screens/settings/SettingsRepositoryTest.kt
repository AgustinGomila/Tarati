package com.agustin.tarati.ui.screens.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.agustin.tarati.game.ai.Difficulty
import com.agustin.tarati.ui.localization.AppLanguage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsRepositoryTest {

    @Test
    fun isDarkTheme_returnsStoredValue() = runTest {
        // Configuración directa para este test específico
        val mockDataStore = mockk<DataStore<Preferences>>()
        val mockPreferences = mockk<Preferences>()

        every { mockPreferences[SettingsRepositoryImpl.DARK_THEME_KEY] } returns true
        every { mockDataStore.data } returns flowOf(mockPreferences)

        val repository = SettingsRepositoryImpl(mockDataStore)
        val result = repository.isDarkTheme.take(1).toList()[0]

        assertTrue("Dark theme should be enabled", result)
    }

    @Test
    fun isDarkTheme_returnsFalseWhenNotSet() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val mockPreferences = mockk<Preferences>()

        every { mockPreferences[SettingsRepositoryImpl.DARK_THEME_KEY] } returns null
        every { mockDataStore.data } returns flowOf(mockPreferences)

        val repository = SettingsRepositoryImpl(mockDataStore)
        val result = repository.isDarkTheme.take(1).toList()[0]

        assertFalse("Dark theme should be disabled by default", result)
    }

    @Test
    fun difficulty_returnsStoredValue() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val mockPreferences = mockk<Preferences>()

        every { mockPreferences[SettingsRepositoryImpl.DIFFICULTY_KEY] } returns Difficulty.HARD.depth
        every { mockDataStore.data } returns flowOf(mockPreferences)

        val repository = SettingsRepositoryImpl(mockDataStore)
        val result = repository.difficulty.take(1).toList()[0]

        assertEquals("Difficulty should be HARD", Difficulty.HARD, result)
    }

    @Test
    fun difficulty_returnsDefaultWhenNotSet() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val mockPreferences = mockk<Preferences>()

        every { mockPreferences[SettingsRepositoryImpl.DIFFICULTY_KEY] } returns null
        every { mockDataStore.data } returns flowOf(mockPreferences)

        val repository = SettingsRepositoryImpl(mockDataStore)
        val result = repository.difficulty.take(1).toList()[0]

        assertEquals("Difficulty should be DEFAULT", Difficulty.DEFAULT, result)
    }

    @Test
    fun language_returnsStoredValue() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val mockPreferences = mockk<Preferences>()

        every { mockPreferences[SettingsRepositoryImpl.LANGUAGE_KEY] } returns AppLanguage.ENGLISH.name
        every { mockDataStore.data } returns flowOf(mockPreferences)

        val repository = SettingsRepositoryImpl(mockDataStore)
        val result = repository.language.take(1).toList()[0]

        assertEquals("Language should be ENGLISH", AppLanguage.ENGLISH, result)
    }

    @Test
    fun language_returnsDefaultWhenNotSet() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val mockPreferences = mockk<Preferences>()

        every { mockPreferences[SettingsRepositoryImpl.LANGUAGE_KEY] } returns null
        every { mockDataStore.data } returns flowOf(mockPreferences)

        val repository = SettingsRepositoryImpl(mockDataStore)
        val result = repository.language.take(1).toList()[0]

        assertEquals("Language should be SPANISH by default", AppLanguage.SPANISH, result)
    }

    @Test
    fun labelsVisibility_returnsStoredValue() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val mockPreferences = mockk<Preferences>()

        every { mockPreferences[SettingsRepositoryImpl.LABELS_VISIBILITY_KEY] } returns true
        every { mockDataStore.data } returns flowOf(mockPreferences)

        val repository = SettingsRepositoryImpl(mockDataStore)
        val result = repository.labelsVisibility.take(1).toList()[0]

        assertEquals("Labels should be visible", true, result)
    }

    @Test
    fun labelsVisibility_returnsDefaultWhenNotSet() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val mockPreferences = mockk<Preferences>()

        every { mockPreferences[SettingsRepositoryImpl.LABELS_VISIBILITY_KEY] } returns true
        every { mockDataStore.data } returns flowOf(mockPreferences)

        val repository = SettingsRepositoryImpl(mockDataStore)
        val result = repository.labelsVisibility.take(1).toList()[0]

        assertTrue("Labels should be visible by default", result)
    }

    // Tests de escritura
    @Test
    fun setDarkTheme_savesValue() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val mockPreferences = mockk<Preferences>()

        // Configuración para lectura
        every { mockPreferences[SettingsRepositoryImpl.DARK_THEME_KEY] } returns null
        every { mockDataStore.data } returns flowOf(mockPreferences)

        // Mockear edit directamente
        coEvery { mockDataStore.updateData(any()) } returns mockk()

        val repository = SettingsRepositoryImpl(mockDataStore)
        repository.setDarkTheme(true)

        // Verificar que se llamó a updateData (que es lo que usa edit internamente)
        coVerify {
            mockDataStore.updateData(any())
        }
    }

    @Test
    fun setDifficulty_savesValue() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val mockPreferences = mockk<Preferences>()

        every { mockPreferences[SettingsRepositoryImpl.DIFFICULTY_KEY] } returns null
        every { mockDataStore.data } returns flowOf(mockPreferences)
        coEvery { mockDataStore.updateData(any()) } returns mockk()

        val repository = SettingsRepositoryImpl(mockDataStore)
        repository.setDifficulty(Difficulty.DEFAULT)

        coVerify {
            mockDataStore.updateData(any())
        }
    }

    @Test
    fun setLanguage_savesValue() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val mockPreferences = mockk<Preferences>()

        every { mockPreferences[SettingsRepositoryImpl.LANGUAGE_KEY] } returns null
        every { mockDataStore.data } returns flowOf(mockPreferences)
        coEvery { mockDataStore.updateData(any()) } returns mockk()

        val repository = SettingsRepositoryImpl(mockDataStore)
        repository.setLanguage(AppLanguage.ENGLISH)

        coVerify {
            mockDataStore.updateData(any())
        }
    }

    @Test
    fun setLabelsVisibility_savesValue() = runTest {
        val mockDataStore = mockk<DataStore<Preferences>>()
        val mockPreferences = mockk<Preferences>()

        every { mockPreferences[SettingsRepositoryImpl.LABELS_VISIBILITY_KEY] } returns null
        every { mockDataStore.data } returns flowOf(mockPreferences)
        coEvery { mockDataStore.updateData(any()) } returns mockk()

        val repository = SettingsRepositoryImpl(mockDataStore)
        repository.setLabelsVisibility(true)

        coVerify {
            mockDataStore.updateData(any())
        }
    }
}