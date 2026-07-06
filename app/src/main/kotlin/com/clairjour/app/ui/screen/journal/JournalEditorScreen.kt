package com.clairjour.app.ui.screen.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clairjour.app.R
import com.clairjour.app.data.AppContainer
import com.clairjour.app.ui.components.viewModelFactoryOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEditorScreen(
    container: AppContainer,
    onDone: () -> Unit
) {
    val vm: JournalEditorViewModel = viewModel(
        factory = viewModelFactoryOf { JournalEditorViewModel(container.journalRepository) }
    )
    val state by vm.state.collectAsState()

    val triggerChoices = listOf("Stress", "Fatigue", "Boredom", "Social", "Sadness", "Anger", "Habit")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.journal_title)) },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Filled.Close, contentDescription = null)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Text(stringResource(R.string.journal_mood), style = MaterialTheme.typography.titleMedium)
            Slider(
                value = state.mood.toFloat(),
                onValueChange = { vm.setMood(it.toInt()) },
                valueRange = 1f..5f,
                steps = 3
            )
            Text(
                "${state.mood}/5",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.align(Alignment.End)
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.notes,
                onValueChange = vm::setNotes,
                label = { Text(stringResource(R.string.journal_notes)) },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            Text(stringResource(R.string.journal_triggers), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                triggerChoices.take(4).forEach { trigger ->
                    FilterChip(
                        selected = state.triggers.contains(trigger),
                        onClick = { vm.toggleTrigger(trigger) },
                        label = { Text(trigger) }
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
            ) {
                triggerChoices.drop(4).forEach { trigger ->
                    FilterChip(
                        selected = state.triggers.contains(trigger),
                        onClick = { vm.toggleTrigger(trigger) },
                        label = { Text(trigger) }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.gratitude,
                onValueChange = vm::setGratitude,
                label = { Text(stringResource(R.string.journal_gratitude)) },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.journal_cravings),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = state.hadCravings,
                    onCheckedChange = vm::setCravings
                )
            }
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { vm.save(onDone) },
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text(stringResource(R.string.action_save)) }

            Spacer(Modifier.height(24.dp))
        }
    }
}
