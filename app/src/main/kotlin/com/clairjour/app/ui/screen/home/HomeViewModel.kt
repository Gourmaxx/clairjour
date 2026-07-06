package com.clairjour.app.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clairjour.app.data.db.AddictionEntity
import com.clairjour.app.data.motivations.Motivation
import com.clairjour.app.data.motivations.MotivationsRepository
import com.clairjour.app.data.repository.AddictionRepository
import com.clairjour.app.data.repository.JournalRepository
import com.clairjour.app.data.repository.PledgeRepository
import com.clairjour.app.domain.Milestone
import com.clairjour.app.domain.Milestones
import com.clairjour.app.domain.Streak
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class HomeUiState(
    val loading: Boolean = true,
    val addictions: List<AddictionEntity> = emptyList(),
    val current: AddictionEntity? = null,
    val streakDays: Int = 0,
    val nextMilestone: Milestone? = null,
    val progressToNext: Float = 0f,
    val pledgeDone: Boolean = false,
    val motivation: Motivation? = null,
    val journalWrittenToday: Boolean = false
)

class HomeViewModel(
    private val addictionRepo: AddictionRepository,
    private val pledgeRepo: PledgeRepository,
    private val journalRepo: JournalRepository,
    private val motivationsRepo: MotivationsRepository
) : ViewModel() {

    private val selectedIdFlow = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = combine(
        addictionRepo.observeActive(),
        selectedIdFlow
    ) { list, selected ->
        list to (list.firstOrNull { it.id == selected } ?: list.firstOrNull())
    }.flatMapLatest { (list, current) ->
        if (current == null) {
            flowOf(HomeUiState(loading = false, addictions = list))
        } else {
            val today = todayLocal()
            combine(
                pledgeRepo.observeFor(current.id, today),
                journalRepo.observeByDate(today)
            ) { pledge, journalToday ->
                val days = Streak.daysSince(current.startDate)
                HomeUiState(
                    loading = false,
                    addictions = list,
                    current = current,
                    streakDays = days,
                    nextMilestone = Milestones.next(days),
                    progressToNext = Milestones.progressToNext(days),
                    pledgeDone = pledge != null,
                    motivation = motivationsRepo.ofDay(),
                    journalWrittenToday = journalToday != null
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    fun select(id: String) {
        selectedIdFlow.value = id
    }

    fun pledge() {
        val current = uiState.value.current ?: return
        if (uiState.value.pledgeDone) return
        viewModelScope.launch {
            pledgeRepo.pledge(current.id, todayLocal())
        }
    }

    private fun todayLocal(): LocalDate =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
}
