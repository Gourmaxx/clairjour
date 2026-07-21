package com.clairjour.app.ui.screen.addiction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clairjour.app.data.db.AddictionEntity
import com.clairjour.app.data.repository.AddictionRepository
import com.clairjour.app.domain.AddictionType
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class AddictionEditUiState(
    val existing: AddictionEntity? = null,
    val name: String = "",
    val type: AddictionType = AddictionType.ALCOHOL,
    val startDate: Instant = Clock.System.now(),
    val costPerDay: String = "",
    val unitPerDay: String = "",
    val unitLabel: String = "",
    val isPrimary: Boolean = false,
    val personalReasons: List<String> = emptyList(),
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
                        personalReasons = entity.personalReasons,
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

    fun addReason(reason: String) {
        val trimmed = reason.trim()
        if (trimmed.isEmpty()) return
        _state.value = _state.value.copy(
            personalReasons = _state.value.personalReasons + trimmed
        )
    }

    fun removeReason(index: Int) {
        val current = _state.value.personalReasons.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _state.value = _state.value.copy(personalReasons = current)
        }
    }

    fun delete(onDone: () -> Unit) {
        val existing = _state.value.existing ?: return
        viewModelScope.launch {
            try {
                addictionRepository.softDelete(existing.id)
                onDone()
            } catch (_: Exception) {
                // delete failure is silent; caller stays on screen
            }
        }
    }

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
                        isPrimary = s.isPrimary,
                        personalReasons = s.personalReasons
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
                            isPrimary = s.isPrimary,
                            personalReasons = s.personalReasons
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
