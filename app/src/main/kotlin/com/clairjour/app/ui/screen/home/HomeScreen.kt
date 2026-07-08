package com.clairjour.app.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.time.Period
import java.time.ZoneId
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clairjour.app.R
import com.clairjour.app.data.AppContainer
import com.clairjour.app.data.db.AddictionEntity
import com.clairjour.app.domain.AddictionType
import com.clairjour.app.ui.components.ClairjourBrandBadge
import com.clairjour.app.ui.components.ClairjourCard
import com.clairjour.app.ui.components.MilestoneProgress
import com.clairjour.app.ui.components.viewModelFactoryOf

@Composable
fun HomeScreen(
    container: AppContainer,
    contentPadding: PaddingValues,
    onOpenAddiction: (String) -> Unit,
    onAddAddiction: () -> Unit,
    onOpenJournalEditor: () -> Unit
) {
    val vm: HomeViewModel = viewModel(
        factory = viewModelFactoryOf {
            HomeViewModel(
                container.addictionRepository,
                container.pledgeRepository,
                container.journalRepository,
                container.motivationsRepository
            )
        }
    )
    val state by vm.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        ClairjourBrandBadge(modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(20.dp))

        if (state.addictions.size > 1) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.addictions, key = { it.id }) { addiction ->
                    val isCurrent = state.current?.id == addiction.id
                    FilterChip(
                        selected = isCurrent,
                        onClick = { vm.select(addiction.id) },
                        label = { Text(addiction.name) }
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        val current = state.current
        if (current == null) {
            EmptyState(onAdd = onAddAddiction)
        } else {
            CounterBlock(
                addiction = current,
                startDate = current.startDate,
                onOpen = { onOpenAddiction(current.id) }
            )
            Spacer(Modifier.height(20.dp))

            state.nextMilestone?.let { next ->
                val remaining = (next.days - state.streakDays).coerceAtLeast(0)
                MilestoneProgress(
                    label = stringResource(
                        R.string.home_next_milestone,
                        stringResource(next.labelRes),
                        remaining
                    ),
                    progress = state.progressToNext
                )
                Spacer(Modifier.height(24.dp))
            }

            PledgeCard(done = state.pledgeDone, onPledge = { vm.pledge() })
            Spacer(Modifier.height(16.dp))

            state.motivation?.let { motivation ->
                MotivationCard(motivation.textFor(LocalConfiguration.current.locales[0].language))
                Spacer(Modifier.height(16.dp))
            }

            JournalQuickCard(
                written = state.journalWrittenToday,
                onOpen = onOpenJournalEditor
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EmptyState(onAdd: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.onboarding_addiction_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onAdd,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) { Text(stringResource(R.string.action_add)) }
    }
}

@Composable
private fun CounterBlock(
    addiction: AddictionEntity,
    startDate: Instant,
    onOpen: () -> Unit
) {
    var now by remember { mutableStateOf(Clock.System.now()) }
    LaunchedEffect(startDate) {
        while (isActive) {
            delay(1_000)
            now = Clock.System.now()
        }
    }

    val totalSeconds = (now - startDate).inWholeSeconds.coerceAtLeast(0)
    val elapsedDays = totalSeconds / 86_400
    val hours = (totalSeconds % 86_400) / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60

    val period = remember(totalSeconds / 86_400) {
        val zone = ZoneId.systemDefault()
        val from = java.time.Instant.ofEpochMilli(startDate.toEpochMilliseconds()).atZone(zone).toLocalDate()
        val to   = java.time.Instant.ofEpochMilli(now.toEpochMilliseconds()).atZone(zone).toLocalDate()
        Period.between(from, to)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
    ) {
        Text(
            text = elapsedDays.toString(),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (elapsedDays == 1L) stringResource(R.string.home_day_label)
                   else stringResource(R.string.home_days_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(6.dp))
        val type = AddictionType.fromName(addiction.type)
        Text(
            text = addiction.name.ifBlank { stringResource(type.labelRes) },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        if (period.years > 0 || period.months > 0) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                if (period.years > 0) {
                    ChronoUnit(value = period.years.toString(), label = pluralStringResource(R.plurals.chrono_years, period.years))
                    ChronoSeparator()
                }
                ChronoUnit(value = period.months.toString(), label = pluralStringResource(R.plurals.chrono_months, period.months))
                ChronoSeparator()
                ChronoUnit(value = period.days.toString(), label = pluralStringResource(R.plurals.chrono_days, period.days))
            }
            Spacer(Modifier.height(10.dp))
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            ChronoUnit(value = "%02d".format(hours), label = "h")
            ChronoSeparator()
            ChronoUnit(value = "%02d".format(minutes), label = "m")
            ChronoSeparator()
            ChronoUnit(value = "%02d".format(seconds), label = "s")
        }
    }
}

@Composable
private fun ChronoUnit(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ChronoSeparator() {
    Text(
        text = ":",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Light,
        color = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 0.dp)
    )
}

@Composable
private fun PledgeCard(done: Boolean, onPledge: () -> Unit) {
    if (done) {
        ClairjourCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(R.string.home_pledge_done),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    } else {
        Button(
            onClick = onPledge,
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(
                stringResource(R.string.home_pledge_cta),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun MotivationCard(text: String) {
    ClairjourCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                stringResource(R.string.home_quote_of_day),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "\" $text \"",
                style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun JournalQuickCard(written: Boolean, onOpen: () -> Unit) {
    Surface(
        onClick = onOpen,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (written) stringResource(R.string.home_journal_edit)
                       else stringResource(R.string.home_journal_write),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "→",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
