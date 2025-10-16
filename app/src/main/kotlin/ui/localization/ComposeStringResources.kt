package com.agustin.tarati.ui.localization

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import java.util.*

@Composable
fun LocalizedText(
    @StringRes id: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = Color.Unspecified,
    textAlign: TextAlign = TextAlign.Start,
    vararg args: Any
) {
    val text = localizedString(id, *args)

    Text(
        text = text,
        modifier = modifier,
        style = style,
        color = color,
        textAlign = textAlign
    )
}

@Composable
fun localizedString(@StringRes id: Int, vararg args: Any): String {
    val context = LocalContext.current
    val locale = LocalAppLanguage.current
    val deviceConfig = LocalConfiguration.current

    // Cache por locale + vertexId + args
    return remember(id, locale, args) {
        getLocalizedString(context, deviceConfig, locale, id, *args)
    }
}

// Función no composable para caching
private val stringCache = mutableMapOf<String, String>()

private fun getLocalizedString(
    context: Context,
    deviceConfig: Configuration,
    locale: Locale,
    @StringRes id: Int,
    vararg args: Any
): String {
    val cacheKey = "${locale.language}_$id" + if (args.isNotEmpty()) "_${args.joinToString("_")}" else ""

    return stringCache.getOrPut(cacheKey) {
        val mergedConfig = Configuration(deviceConfig).apply {
            setLocale(locale)
        }

        val localizedContext = context.createConfigurationContext(mergedConfig)
        if (args.isNotEmpty()) {
            localizedContext.resources.getString(id, *args)
        } else {
            localizedContext.resources.getString(id)
        }
    }
}