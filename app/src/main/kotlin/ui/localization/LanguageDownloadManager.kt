package com.agustin.tarati.ui.localization

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.play.core.splitinstall.SplitInstallException
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import kotlinx.coroutines.delay
import java.io.Serializable
import java.util.*


class LanguageDownloadManager(private val context: Context) {
    private val splitInstallManager by lazy {
        SplitInstallManagerFactory.create(context)
    }

    /**
     * Verificar si un idioma está disponible
     * Retornar true para el idioma base y los idiomas descargados
     */
    fun isLanguageDownloaded(locale: Locale): Boolean {
        // El idioma base (inglés o el idioma por defecto) siempre está disponible
        if (isBaseLanguage(locale)) {
            return true
        }

        // Verificar si está en la lista de idiomas instalados
        return splitInstallManager.installedLanguages.contains<Serializable>(locale)
    }

    fun downloadLanguage(
        locale: Locale,
        onSuccess: () -> Unit,
        onError: (Int) -> Unit,
        onProgress: (Int) -> Unit = {}
    ) {
        // Si ya está disponible, éxito inmediato
        if (isLanguageDownloaded(locale)) {
            onSuccess()
            return
        }

        val request = SplitInstallRequest.newBuilder()
            .addLanguage(locale)
            .build()

        var listener: SplitInstallStateUpdatedListener? = null

        listener = SplitInstallStateUpdatedListener { state ->
            when (state.status()) {
                SplitInstallSessionStatus.INSTALLED -> {
                    onSuccess()
                    listener?.let { splitInstallManager.unregisterListener(it) }
                }

                SplitInstallSessionStatus.FAILED -> {
                    onError(state.errorCode())
                    listener?.let { splitInstallManager.unregisterListener(it) }
                }

                SplitInstallSessionStatus.DOWNLOADING -> {
                    val progress = ((state.bytesDownloaded() * 100) / state.totalBytesToDownload()).toInt()
                    onProgress(progress)
                }
            }
        }

        splitInstallManager.registerListener(listener)

        // Iniciar la descarga
        splitInstallManager.startInstall(request)
            .addOnSuccessListener { _ ->
                // La descarga se inició correctamente
                // El progreso se maneja en el listener
            }
            .addOnFailureListener { exception ->
                val errorCode = if (exception is SplitInstallException) {
                    exception.errorCode
                } else {
                    exception.hashCode()
                }
                onError(errorCode)
                listener.let { splitInstallManager.unregisterListener(it) }
            }
    }

    @Composable
    fun useLanguageDownload(locale: Locale): Pair<LanguageDownloadState, () -> Unit> {
        val context = LocalContext.current
        val downloadManager = remember { LanguageDownloadManager(context) }

        var state by remember(locale) {
            mutableStateOf(
                if (downloadManager.isLanguageDownloaded(locale)) {
                    LanguageDownloadState.Downloaded
                } else {
                    LanguageDownloadState.NotDownloaded
                }
            )
        }

        val downloadLanguage: () -> Unit = {
            if (state !is LanguageDownloadState.Downloading) {
                state = LanguageDownloadState.Downloading
                downloadManager.downloadLanguage(
                    locale = locale,
                    onSuccess = {
                        // La verificación periódica detectará el cambio
                    },
                    onError = { errorCode ->
                        state = LanguageDownloadState.Error(errorCode)
                    }
                )
            }
        }

        // Verificar periódicamente
        LaunchedEffect(locale, state) {
            if (state is LanguageDownloadState.Downloading) {
                while (state is LanguageDownloadState.Downloading) {
                    delay(1000)
                    if (downloadManager.isLanguageDownloaded(locale)) {
                        state = LanguageDownloadState.Downloaded
                        break
                    }
                }
            }
        }

        return Pair(state, downloadLanguage)
    }

    sealed interface LanguageDownloadState {
        val isDownloaded: Boolean
        val isDownloading: Boolean
        val errorCode: Int?
        fun download()

        object Downloaded : LanguageDownloadState {
            override val isDownloaded = true
            override val isDownloading = false
            override val errorCode = null
            override fun download() {}
        }

        object NotDownloaded : LanguageDownloadState {
            override val isDownloaded = false
            override val isDownloading = false
            override val errorCode = null
            override fun download() {}
        }

        object Downloading : LanguageDownloadState {
            override val isDownloaded = false
            override val isDownloading = true
            override val errorCode = null
            override fun download() {}
        }

        class Error(error: Int) : LanguageDownloadState {
            override val isDownloaded = false
            override val isDownloading = false
            override val errorCode = error
            override fun download() {}
        }
    }

    /**
     * Verificar si es el idioma base de la app
     */
    private fun isBaseLanguage(locale: Locale): Boolean {
        // Asumiendo que tu idioma base es inglés
        // Ajusta según tu configuración
        return locale.language == "en"
    }
}