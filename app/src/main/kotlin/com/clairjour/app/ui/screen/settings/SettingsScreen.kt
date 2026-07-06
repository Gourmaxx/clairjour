package com.clairjour.app.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clairjour.app.R
import com.clairjour.app.data.AppContainer
import com.clairjour.app.data.prefs.AppLanguage
import com.clairjour.app.data.prefs.LocaleManager
import com.clairjour.app.data.prefs.ThemeMode
import com.clairjour.app.domain.AddictionType
import com.clairjour.app.notifications.ReminderScheduler
import com.clairjour.app.ui.components.viewModelFactoryOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    container: AppContainer,
    contentPadding: PaddingValues,
    onEditAddiction: (String) -> Unit,
    onAddAddiction: () -> Unit
) {
    val vm: SettingsViewModel = viewModel(
        factory = viewModelFactoryOf {
            SettingsViewModel(container.settingsRepository, container.addictionRepository)
        }
    )
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        Section(title = stringResource(R.string.settings_language)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppLanguage.entries.forEach { lang ->
                    FilterChip(
                        selected = state.language == lang,
                        onClick = {
                            vm.setLanguage(lang)
                            LocaleManager.apply(lang)
                        },
                        label = {
                            Text(
                                when (lang) {
                                    AppLanguage.SYSTEM -> stringResource(R.string.settings_theme_system)
                                    AppLanguage.ENGLISH -> "English"
                                    AppLanguage.FRENCH -> "Français"
                                }
                            )
                        }
                    )
                }
            }
        }

        Section(title = stringResource(R.string.settings_theme)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.themeMode == ThemeMode.SYSTEM,
                    onClick = { vm.setTheme(ThemeMode.SYSTEM) },
                    label = { Text(stringResource(R.string.settings_theme_system)) }
                )
                FilterChip(
                    selected = state.themeMode == ThemeMode.LIGHT,
                    onClick = { vm.setTheme(ThemeMode.LIGHT) },
                    label = { Text(stringResource(R.string.settings_theme_light)) }
                )
                FilterChip(
                    selected = state.themeMode == ThemeMode.DARK,
                    onClick = { vm.setTheme(ThemeMode.DARK) },
                    label = { Text(stringResource(R.string.settings_theme_dark)) }
                )
            }
        }

        Section(title = stringResource(R.string.settings_notifications)) {
            ToggleRow(
                label = stringResource(R.string.settings_notif_pledge),
                checked = state.notifPledgeEnabled,
                onCheckedChange = { enabled ->
                    vm.setPledgeEnabled(enabled)
                    if (enabled) ReminderScheduler.schedulePledge(context, state.pledgeTime.first, state.pledgeTime.second)
                    else ReminderScheduler.cancelPledge(context)
                }
            )
            if (state.notifPledgeEnabled) {
                NotifTimeRow(
                    hour = state.pledgeTime.first,
                    minute = state.pledgeTime.second,
                    onTimeChange = { h, m ->
                        vm.setNotifPledgeTime(h, m)
                        ReminderScheduler.schedulePledge(context, h, m)
                    }
                )
            }
            Spacer(Modifier.height(4.dp))
            ToggleRow(
                label = stringResource(R.string.settings_notif_journal),
                checked = state.notifJournalEnabled,
                onCheckedChange = { enabled ->
                    vm.setJournalEnabled(enabled)
                    if (enabled) ReminderScheduler.scheduleJournal(context, state.journalTime.first, state.journalTime.second)
                    else ReminderScheduler.cancelJournal(context)
                }
            )
            if (state.notifJournalEnabled) {
                NotifTimeRow(
                    hour = state.journalTime.first,
                    minute = state.journalTime.second,
                    onTimeChange = { h, m ->
                        vm.setNotifJournalTime(h, m)
                        ReminderScheduler.scheduleJournal(context, h, m)
                    }
                )
            }
        }

        Section(title = stringResource(R.string.settings_addictions)) {
            state.addictions.forEach { addiction ->
                Surface(
                    onClick = { onEditAddiction(addiction.id) },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(addiction.name, style = MaterialTheme.typography.titleMedium)
                            val type = AddictionType.fromName(addiction.type)
                            Text(
                                stringResource(type.labelRes),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (addiction.isPrimary) {
                            Text(
                                "★",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        } else {
                            TextButton(onClick = { vm.makePrimary(addiction.id) }) {
                                Text("★", color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onAddAddiction,
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.action_add))
            }
        }

        Section(title = stringResource(R.string.settings_about)) {
            Text(
                "Clairjour · v0.1.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(10.dp))
        content()
        Spacer(Modifier.height(6.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotifTimeRow(hour: Int, minute: Int, onTimeChange: (Int, Int) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "%02d:%02d".format(hour, minute),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = { showPicker = true }) {
            Text(stringResource(R.string.action_edit))
        }
    }

    if (showPicker) {
        val pickerState = rememberTimePickerState(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onTimeChange(pickerState.hour, pickerState.minute)
                    showPicker = false
                }) { Text(stringResource(R.string.action_done)) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
            text = { TimePicker(state = pickerState) }
        )
    }
}

