package com.clairjour.app.ui.screen.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clairjour.app.data.backup.BackupRepository
import com.clairjour.app.data.db.AddictionEntity
import com.clairjour.app.data.prefs.AppLanguage
import com.clairjour.app.data.prefs.SettingsRepository
import com.clairjour.app.data.prefs.ThemeMode
import com.clairjour.app.data.repository.AddictionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class BackupStatus { IDLE, LOADING, SUCCESS_EXPORT, SUCCESS_IMPORT, ERROR_EXPORT, ERROR_IMPORT }

data class SettingsUiState(
    val language: AppLanguage = AppLanguage.SYSTEM,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notifPledgeEnabled: Boolean = true,
    val notifJournalEnabled: Boolean = true,
    val pledgeTime: Pair<Int, Int> = 8 to 0,
    val journalTime: Pair<Int, Int> = 21 to 0,
    val addictions: List<AddictionEntity> = emptyList(),
    val backupStatus: BackupStatus = BackupStatus.IDLE
)

class SettingsViewModel(
    private val settings: SettingsRepository,
    private val addictions: AddictionRepository,
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _backupStatus = MutableStateFlow(BackupStatus.IDLE)

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
        .combine(_backupStatus) { s, status -> s.copy(backupStatus = status) }
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

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            _backupStatus.value = BackupStatus.LOADING
            runCatching { backupRepository.export(uri) }
                .onSuccess { _backupStatus.value = BackupStatus.SUCCESS_EXPORT }
                .onFailure { _backupStatus.value = BackupStatus.ERROR_EXPORT }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            _backupStatus.value = BackupStatus.LOADING
            runCatching { backupRepository.import(uri) }
                .onSuccess { _backupStatus.value = BackupStatus.SUCCESS_IMPORT }
                .onFailure { _backupStatus.value = BackupStatus.ERROR_IMPORT }
        }
    }

    fun clearBackupStatus() { _backupStatus.value = BackupStatus.IDLE }
}
