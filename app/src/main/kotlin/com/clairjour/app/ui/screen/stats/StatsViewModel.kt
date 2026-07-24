package com.clairjour.app.ui.screen.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clairjour.app.data.db.AddictionEntity
import com.clairjour.app.data.db.MilestoneDao
import com.clairjour.app.data.db.RelapseDao
import com.clairjour.app.data.repository.AddictionRepository
import com.clairjour.app.data.repository.JournalRepository
import com.clairjour.app.domain.Streak
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class StatsUiState(
    val totalDays: Int = 0,
    val totalSaved: Double = 0.0,
    val milestonesReached: Int = 0,
    val reachedMilestoneDays: Set<Int> = emptySet(),
    val recentMoodPoints: List<Int> = emptyList(),
    val addictions: List<AddictionEntity> = emptyList(),
    val currentStreakDays: Int = 0,
    val recordStreakDays: Int = 0,
    val moodAverage7d: Double? = null,
    val moodAverage30d: Double? = null,
    val topTriggers: List<TriggerCount> = emptyList()
)

data class TriggerCount(val key: String, val count: Int)

class StatsViewModel(
    private val addictions: AddictionRepository,
    private val journal: JournalRepository,
    private val milestoneDao: MilestoneDao,
    private val relapseDao: RelapseDao
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<StatsUiState> = addictions.observeActive().flatMapLatest { list ->
        val primary = list.firstOrNull { it.isPrimary } ?: list.firstOrNull()
        // Relapses only make sense for a chosen primary; when there's none, feed an empty list.
        val relapseFlow = primary?.let { relapseDao.observeFor(it.id) } ?: flowOf(emptyList())
        combine(
            journal.observeRecent(30),
            milestoneDao.observeAll(),
            relapseFlow
        ) { entries, milestones, relapses ->
            val currentStreak = primary?.let { Streak.daysSince(it.startDate) } ?: 0
            val historicalMax = relapses.maxOfOrNull { it.previousStreakDays } ?: 0
            val recordStreak = maxOf(currentStreak, historicalMax)

            val totalDays = list.sumOf { Streak.daysSince(it.startDate).toLong() }.toInt()
            val totalSaved = list.sumOf { (it.costPerDay ?: 0.0) * Streak.daysSince(it.startDate) }
            val moodPoints = entries.map { it.mood }.reversed()

            val avg7 = entries.take(7).map { it.mood }.average().takeIf { !it.isNaN() }
            val avg30 = entries.map { it.mood }.average().takeIf { !it.isNaN() }
            val triggers = entries
                .flatMap { it.triggers }
                .groupingBy { it }
                .eachCount()
                .toList()
                .sortedByDescending { it.second }
                .take(3)
                .map { TriggerCount(it.first, it.second) }

            StatsUiState(
                totalDays = totalDays,
                totalSaved = totalSaved,
                milestonesReached = milestones.size,
                reachedMilestoneDays = milestones.map { it.milestoneDays }.toSet(),
                recentMoodPoints = moodPoints,
                addictions = list,
                currentStreakDays = currentStreak,
                recordStreakDays = recordStreak,
                moodAverage7d = avg7,
                moodAverage30d = avg30,
                topTriggers = triggers
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())
}
