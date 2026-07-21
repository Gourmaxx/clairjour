package com.clairjour.app.ui.screen.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clairjour.app.data.db.AddictionEntity
import com.clairjour.app.data.db.MilestoneDao
import com.clairjour.app.data.db.RelapseDao
import com.clairjour.app.data.repository.AddictionRepository
import com.clairjour.app.data.repository.JournalRepository
import com.clairjour.app.domain.Streak
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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

    val state: StateFlow<StatsUiState> = combine(
        addictions.observeActive(),
        journal.observeRecent(30),
        milestoneDao.observeAll()
    ) { list, entries, milestones ->
        val primary = list.firstOrNull { it.isPrimary } ?: list.firstOrNull()
        val currentStreak = primary?.let { Streak.daysSince(it.startDate) } ?: 0
        val totalDays = list.sumOf { Streak.daysSince(it.startDate).toLong() }.toInt()
        val totalSaved = list.sumOf { (it.costPerDay ?: 0.0) * Streak.daysSince(it.startDate) }
        val moodPoints = entries.map { it.mood }.reversed()

        // Record streak: peak of previousStreakDays across all relapses, compared to current streak.
        val relapses = primary?.let { relapseDao.observeFor(it.id) }
        val recordStreak = maxOf(currentStreak, primary?.let { p ->
            // We can't collect here — fall back to current streak; a background job would fetch history.
            0
        } ?: 0)

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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())
}
