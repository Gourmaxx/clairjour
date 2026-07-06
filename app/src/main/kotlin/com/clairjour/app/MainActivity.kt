package com.clairjour.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.clairjour.app.data.prefs.AppLanguage
import com.clairjour.app.data.prefs.ThemeMode
import com.clairjour.app.ui.ClairjourApp
import com.clairjour.app.ui.WithLocale
import com.clairjour.app.ui.theme.ClairjourTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as ClairjourApplication).container

        setContent {
            val themeMode by container.settingsRepository
                .themeModeFlow
                .collectAsState(initial = ThemeMode.SYSTEM)
            val language by container.settingsRepository
                .languageFlow
                .collectAsState(initial = AppLanguage.SYSTEM)

            ClairjourTheme(themeMode = themeMode) {
                WithLocale(language = language) {
                    ClairjourApp(container = container)
                }
            }
        }
    }
}
