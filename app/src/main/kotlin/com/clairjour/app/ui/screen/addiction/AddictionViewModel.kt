package com.clairjour.app.ui.screen.addiction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clairjour.app.data.db.AddictionEntity
import com.clairjour.app.data.db.MilestoneReachedEntity
import com.clairjour.app.data.repository.AddictionRepository
import com.clairjour.app.data.repository.RelapseRepository
import com.clairjour.app.data.db.MilestoneDao
import com.clairjour.app.domain.AddictionType
import com.clairjour.app.domain.Milestone
import com.clairjour.app.domain.Milestones
import com.clairjour.app.domain.Streak
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class AddictionDetailUiState(
    val addiction: AddictionEntity? = null,
    val streakDays: Int = 0,
    val reachedMilestones: Set<Int> = emptySet(),
    val savedAmount: Double = 0.0,
    val unitsAvoided: Double = 0.0
)

class AddictionDetailViewModel(
    private val addictionRepository: AddictionRepository,
    private val milestoneDao: MilestoneDao,
    private val relapseRepository: RelapseRepository,
    private val addictionId: String
) : ViewModel() {

    val state: StateFlow<AddictionDetailUiState> = combine(
        addictionRepository.observeById(addictionId),
        milestoneDao.observeFor(addictionId)
    ) { addiction, milestones ->
        if (addiction == null) AddictionDetailUiState()
        else {
            val days = Streak.daysSince(addiction.startDate)
            AddictionDetailUiState(
                addiction = addiction,
                streakDays = days,
                reachedMilestones = milestones.map { it.milestoneDays }.toSet(),
                savedAmount = (addiction.costPerDay ?: 0.0) * days,
                unitsAvoided = (addiction.unitPerDay ?: 0.0) * days
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AddictionDetailUiState())

    fun reportRelapse(note: String?) {
        viewModelScope.launch {
            relapseRepository.reportRelapse(addictionId, note, emptyList())
        }
    }
}

data class AddictionEditUiState(
    val existing: AddictionEntity? = null,
    val name: String = "",
    val type: AddictionType = AddictionType.ALCOHOL,
    val startDate: Instant = Clock.System.now(),
    val costPerDay: String = "",
    val unitPerDay: String = "",
    val unitLabel: String = "",
    val isPrimary: Boolean = false,
    val loaded: Boolean = false
)

class AddictionEditViewModel(
    private val addictionRepository: AddictionRepository,
    private val existingId: String?
) : ViewModel() {

    private val _state = kotlinx.coroutines.flow.MutableStateFlow(AddictionEditUiState())
    val state: StateFlow<AddictionEditUiState> = _state

    init {
        if (existingId != null) {
            viewModelScope.launch {
                val entity = addictionRepository.getById(existingId)
                if (entity != null) {
                    _state.value = AddictionEditUiState(
                        existing = entity,
                        name = entity.name,
                        type = AddictionType.fromName(entity.type),
                        startDate = entity.startDate,
                        costPerDay = entity.costPerDay?.toString() ?: "",
                        unitPerDay = entity.unitPerDay?.toString() ?: "",
                        unitLabel = entity.unitLabel.orEmpty(),
                        isPrimary = entity.isPrimary,
                        loaded = true
                    )
                } else {
                    _state.value = _state.value.copy(loaded = true)
                }
            }
        } else {
            _state.value = _state.value.copy(loaded = true)
        }
    }

    fun setName(v: String) { _state.value = _state.value.copy(name = v) }
    fun setType(v: AddictionType) { _state.value = _state.value.copy(type = v) }
    fun setStartDate(v: Instant) { _state.value = _state.value.copy(startDate = v) }
    fun setCost(v: String) {
        _state.value = _state.value.copy(costPerDay = v.filter { it.isDigit() || it == '.' || it == ',' })
    }
    fun setUnit(v: String) {
        _state.value = _state.value.copy(unitPerDay = v.filter { it.isDigit() || it == '.' || it == ',' })
    }
    fun setUnitLabel(v: String) { _state.value = _state.value.copy(unitLabel = v) }
    fun setPrimary(v: Boolean) { _state.value = _state.value.copy(isPrimary = v) }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        viewModelScope.launch {
            try {
                if (s.existing == null) {
                    addictionRepository.create(
                        name = s.name.ifBlank { "?" },
                        type = s.type,
                        startDate = s.startDate,
                        costPerDay = s.costPerDay.replace(',', '.').toDoubleOrNull(),
                        unitPerDay = s.unitPerDay.replace(',', '.').toDoubleOrNull(),
                        unitLabel = s.unitLabel.ifBlank { null },
                        isPrimary = s.isPrimary
                    )
                } else {
                    addictionRepository.update(
                        s.existing.copy(
                            name = s.name.ifBlank { "?" },
                            type = s.type.name,
                            startDate = s.startDate,
                            costPerDay = s.costPerDay.replace(',', '.').toDoubleOrNull(),
                            unitPerDay = s.unitPerDay.replace(',', '.').toDoubleOrNull(),
                            unitLabel = s.unitLabel.ifBlank { null },
                            isPrimary = s.isPrimary
                        )
                    )
                }
                onDone()
            } catch (_: Exception) {
                // save failure is silent; form remains open
            }
        }
    }
}
