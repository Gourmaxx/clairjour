package com.clairjour.app.data.prefs

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LocaleManager {
    fun apply(language: AppLanguage) {
        val tags = when (language) {
            AppLanguage.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
            AppLanguage.ENGLISH -> LocaleListCompat.forLanguageTags("en")
            AppLanguage.FRENCH -> LocaleListCompat.forLanguageTags("fr")
        }
        AppCompatDelegate.setApplicationLocales(tags)
    }
}
