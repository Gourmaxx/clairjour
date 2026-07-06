package com.clairjour.app.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clairjour.app.data.db.AddictionEntity
import com.clairjour.app.data.prefs.AppLanguage
import com.clairjour.app.data.prefs.SettingsRepository
import com.clairjour.app.data.prefs.ThemeMode
import com.clairjour.app.data.repository.AddictionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val language: AppLanguage = AppLanguage.SYSTEM,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notifPledgeEnabled: Boolean = true,
    val notifJournalEnabled: Boolean = true,
    val pledgeTime: Pair<Int, Int> = 8 to 0,
    val journalTime: Pair<Int, Int> = 21 to 0,
    val addictions: List<AddictionEntity> = emptyList()
)

class SettingsViewModel(
    private val settings: SettingsRepository,
    private val addictions: AddictionRepository
) : ViewModel() {

    val state: StateFlow<SettingsUiState> = combine(
        settings.languageFlow,
        settings.themeModeFlow,
        settings.notifPledgeEnabledFlow,
        settings.notifJournalEnabledFlow,
        addictions.observeActive()
    ) { lang, theme, pledge, journal, addicts ->
        SettingsUiState(
            language = lang,
            themeMode = theme,
            notifPledgeEnabled = pledge,
            notifJournalEnabled = journal,
            addictions = addicts
        )
    }
        .combine(settings.notifPledgeTimeFlow) { s, t -> s.copy(pledgeTime = t) }
        .combine(settings.notifJournalTimeFlow) { s, t -> s.copy(journalTime = t) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setLanguage(v: AppLanguage) { viewModelScope.launch { settings.setLanguage(v) } }
    fun setTheme(v: ThemeMode) { viewModelScope.launch { settings.setThemeMode(v) } }
    fun setPledgeEnabled(v: Boolean) {
        viewModelScope.launch { settings.setNotifPledgeEnabled(v) }
    }
    fun setJournalEnabled(v: Boolean) {
        viewModelScope.launch { settings.setNotifJournalEnabled(v) }
    }
    fun setNotifPledgeTime(hour: Int, minute: Int) {
        viewModelScope.launch { settings.setNotifPledgeTime(hour, minute) }
    }
    fun setNotifJournalTime(hour: Int, minute: Int) {
        viewModelScope.launch { settings.setNotifJournalTime(hour, minute) }
    }
    fun deleteAddiction(id: String) {
        viewModelScope.launch { addictions.softDelete(id) }
    }
    fun makePrimary(id: String) {
        viewModelScope.launch { addictions.markPrimary(id) }
    }
}
