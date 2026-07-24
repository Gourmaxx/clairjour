package com.clairjour.app.ui.screen.crisis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clairjour.app.data.repository.AddictionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CrisisUiState(
    val reasons: List<String> = emptyList()
)

/**
 * Feeds [CrisisScreen] with the "why" of a specific addiction — the one the user was
 * looking at when they tapped the crisis FAB. If [scopedAddictionId] is null, falls back
 * to the primary (or any) active addiction so the screen still works from cold entry.
 */
class CrisisViewModel(
    private val addictionRepository: AddictionRepository,
    private val scopedAddictionId: String? = null
) : ViewModel() {

    private val _state = MutableStateFlow(CrisisUiState())
    val state: StateFlow<CrisisUiState> = _state

    init {
        viewModelScope.launch {
            addictionRepository.observeActive().collect { addictions ->
                val target = scopedAddictionId?.let { id -> addictions.firstOrNull { it.id == id } }
                    ?: addictions.firstOrNull { it.isPrimary }
                    ?: addictions.firstOrNull()
                _state.value = CrisisUiState(reasons = target?.personalReasons.orEmpty())
            }
        }
    }
}
