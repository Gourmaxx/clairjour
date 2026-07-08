package com.clairjour.app.ui.screen.addiction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.clairjour.app.domain.AddictionType
import com.clairjour.app.domain.Formatters
import com.clairjour.app.domain.Milestones
import com.clairjour.app.ui.components.ClairjourCard
import com.clairjour.app.ui.components.viewModelFactoryOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddictionDetailScreen(
    container: AppContainer,
    addictionId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current
    val vm: AddictionDetailViewModel = viewModel(
        factory = viewModelFactoryOf {
            AddictionDetailViewModel(
                container.addictionRepository,
                container.milestoneDao,
                container.relapseRepository,
                addictionId
            )
        }
    )
    val state by vm.state.collectAsState()
    var showRelapseDialog by remember { mutableStateOf(false) }
    var relapseNote by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(state.addiction?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = null)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val addiction = state.addiction ?: return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            val type = AddictionType.fromName(addiction.type)
            Text(
                stringResource(type.labelRes).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                state.streakDays.toString(),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                if (state.streakDays == 1) stringResource(R.string.home_day_label)
                else stringResource(R.string.home_days_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                if ((addiction.costPerDay ?: 0.0) > 0) {
                    StatSmallCard(
                        label = stringResource(R.string.detail_saved),
                        value = Formatters.currency(state.savedAmount, context),
                        modifier = Modifier.weight(1f)
                    )
                }
                if ((addiction.unitPerDay ?: 0.0) > 0) {
                    val unit = addiction.unitLabel.orEmpty()
                    StatSmallCard(
                        label = stringResource(R.string.detail_units_avoided),
                        value = String.format("%.0f %s", state.unitsAvoided, unit),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(R.string.detail_milestones),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Milestones.all.forEach { m ->
                    val reached = m.days in state.reachedMilestones
                    MilestoneRow(
                        label = stringResource(m.labelRes),
                        reached = reached
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
            TextButton(
                onClick = { showRelapseDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.detail_report_relapse),
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }

    if (showRelapseDialog) {
        AlertDialog(
            onDismissRequest = { showRelapseDialog = false },
            title = { Text(stringResource(R.string.detail_relapse_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.detail_relapse_body))
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = relapseNote,
                        onValueChange = { relapseNote = it },
                        label = { Text(stringResource(R.string.detail_relapse_note)) },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.reportRelapse(relapseNote.ifBlank { null })
                    showRelapseDialog = false
                    onBack()
                }) { Text(stringResource(R.string.action_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showRelapseDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun StatSmallCard(label: String, value: String, modifier: Modifier = Modifier) {
    ClairjourCard(modifier = modifier) {
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun MilestoneRow(label: String, reached: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(
                    if (reached) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.outlineVariant,
                    CircleShape
                )
        )
        Spacer(Modifier.width(12.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (reached) MaterialTheme.colorScheme.onBackground
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
