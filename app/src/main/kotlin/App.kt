package com.agustin.tarati

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.agustin.tarati.ui.components.board.BoardAnimationViewModel
import com.agustin.tarati.ui.components.board.BoardSelectionViewModel
import com.agustin.tarati.ui.screens.main.MainViewModel
import com.agustin.tarati.ui.screens.settings.SettingsRepository
import com.agustin.tarati.ui.screens.settings.SettingsRepositoryImpl
import com.agustin.tarati.ui.screens.settings.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import java.io.File

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(
                dataStoreModule,
                repositoryModule,
                viewModelModule,
            )
        }
    }

    // Módulo de DataStore
    private val dataStoreModule = module {
        single<DataStore<Preferences>> {
            PreferenceDataStoreFactory.create(
                produceFile = {
                    File(get<Context>().filesDir, "datastore/user_preferences.preferences_pb")
                }
            )
        }
    }

    // Módulo de repositorios
    private val repositoryModule = module {
        single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    }

    // Módulo de ViewModels
    private val viewModelModule = module {
        viewModel { SettingsViewModel(getKoin().get()) }
        viewModel { MainViewModel(getKoin().get()) }
        viewModel { BoardAnimationViewModel() }
        viewModel { BoardSelectionViewModel() }
    }
}