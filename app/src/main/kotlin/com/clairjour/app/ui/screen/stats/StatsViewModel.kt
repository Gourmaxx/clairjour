package com.clairjour.app.ui.screen.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clairjour.app.data.db.AddictionEntity
import com.clairjour.app.data.db.JournalEntryEntity
import com.clairjour.app.data.db.MilestoneDao
import com.clairjour.app.data.repository.AddictionRepository
import com.clairjour.app.data.repository.JournalRepository
import com.clairjour.app.domain.Streak
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class StatsUiState(
    val totalDays: Int = 0,
    val totalSaved: Double = 0.0,
    val milestonesReached: Int = 0,
    val recentMoodPoints: List<Int> = emptyList(),
    val addictions: List<AddictionEntity> = emptyList()
)

class StatsViewModel(
    private val addictions: AddictionRepository,
    private val journal: JournalRepository,
    private val milestoneDao: MilestoneDao
) : ViewModel() {

    val state: StateFlow<StatsUiState> = combine(
        addictions.observeActive(),
        journal.observeRecent(30),
        milestoneDao.countAll()
    ) { list, entries, milestoneCount ->
        val totalDays = list.sumOf { Streak.daysSince(it.startDate).toLong() }.toInt()
        val totalSaved = list.sumOf { (it.costPerDay ?: 0.0) * Streak.daysSince(it.startDate) }
        val moodPoints = entries.map { it.mood }.reversed()
        StatsUiState(
            totalDays = totalDays,
            totalSaved = totalSaved,
            milestonesReached = milestoneCount,
            recentMoodPoints = moodPoints,
            addictions = list
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())
}
