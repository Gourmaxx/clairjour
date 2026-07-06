package com.clairjour.app.ui

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.clairjour.app.data.prefs.AppLanguage
import java.util.Locale

/**
 * Wraps [base] so that resource lookups use [locale] while keeping the original context
 * reachable via baseContext — this preserves the ContextWrapper chain that Compose uses
 * to find the Activity (ActivityResultRegistryOwner, LifecycleOwner, ViewModelStoreOwner…).
 */
private class LocaleContextWrapper(base: Context, locale: Locale) : ContextWrapper(base) {
    val localizedConfig: Configuration = Configuration(base.resources.configuration).apply {
        setLocale(locale)
        setLayoutDirection(locale)
    }
    private val localizedResources: Resources =
        base.createConfigurationContext(localizedConfig).resources

    override fun getResources(): Resources = localizedResources
}

@Composable
fun WithLocale(language: AppLanguage, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val wrapped = remember(language, configuration) {
        val target = when (language) {
            AppLanguage.SYSTEM -> Locale.getDefault()
            AppLanguage.ENGLISH -> Locale.ENGLISH
            AppLanguage.FRENCH -> Locale.FRENCH
        }
        LocaleContextWrapper(context, target)
    }

    CompositionLocalProvider(
        LocalContext provides wrapped,
        LocalConfiguration provides wrapped.localizedConfig
    ) {
        content()
    }
}
