package com.clairjour.app.ui.screen.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clairjour.app.R
import com.clairjour.app.data.AppContainer
import com.clairjour.app.domain.Formatters
import com.clairjour.app.domain.Milestones
import com.clairjour.app.ui.components.ClairjourCard
import com.clairjour.app.ui.components.viewModelFactoryOf

@Composable
fun StatsScreen(
    container: AppContainer,
    contentPadding: PaddingValues
) {
    val context = LocalContext.current
    val vm: StatsViewModel = viewModel(
        factory = viewModelFactoryOf {
            StatsViewModel(
                container.addictionRepository,
                container.journalRepository,
                container.milestoneDao
            )
        }
    )
    val state by vm.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(stringResource(R.string.stats_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard(
                title = stringResource(R.string.stats_total_days),
                value = state.totalDays.toString(),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.stats_milestones_reached),
                value = state.milestonesReached.toString(),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))

        StatCard(
            title = stringResource(R.string.stats_total_saved),
            value = Formatters.currency(state.totalSaved, context),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))

        Text(stringResource(R.string.stats_mood_over_time), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        MoodChart(points = state.recentMoodPoints)
        Spacer(Modifier.height(24.dp))

        Text(stringResource(R.string.detail_milestones), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Milestones.all.forEach { m ->
                val reached = m.days in state.reachedMilestoneDays
                MilestoneRow(
                    label = stringResource(m.labelRes),
                    reached = reached
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    ClairjourCard(modifier = modifier) {
        Column {
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
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

@Composable
private fun MoodChart(points: List<Int>) {
    val stroke = MaterialTheme.colorScheme.secondary
    val axis = MaterialTheme.colorScheme.outlineVariant
    ClairjourCard(modifier = Modifier.fillMaxWidth()) {
        if (points.isEmpty()) {
            Text(
                "—",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val maxY = 5f
                val minY = 1f
                val pad = 8f
                val w = size.width - pad * 2
                val h = size.height - pad * 2
                val step = if (points.size > 1) w / (points.size - 1) else w
                var prev: Offset? = null
                points.forEachIndexed { i, mood ->
                    val x = pad + step * i
                    val ratio = (mood - minY) / (maxY - minY)
                    val y = pad + h - h * ratio
                    val p = Offset(x, y)
                    if (prev != null) {
                        drawLine(color = stroke, start = prev!!, end = p, strokeWidth = 4f)
                    }
                    drawCircle(color = stroke, radius = 6f, center = p)
                    prev = p
                }
                drawLine(
                    color = axis,
                    start = Offset(pad, size.height - pad),
                    end = Offset(size.width - pad, size.height - pad),
                    strokeWidth = 1f
                )
            }
        }
    }
}
