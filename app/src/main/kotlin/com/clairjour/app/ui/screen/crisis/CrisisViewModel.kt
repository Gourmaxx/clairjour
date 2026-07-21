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
 * Feeds [CrisisScreen] with the user's personal reasons pulled from their primary or first
 * active addiction. Kept read-only intentionally — the crisis screen is not the place to edit.
 */
class CrisisViewModel(
    private val addictionRepository: AddictionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CrisisUiState())
    val state: StateFlow<CrisisUiState> = _state

    init {
        viewModelScope.launch {
            addictionRepository.observeActive().collect { addictions ->
                val primary = addictions.firstOrNull { it.isPrimary } ?: addictions.firstOrNull()
                _state.value = CrisisUiState(reasons = primary?.personalReasons.orEmpty())
            }
        }
    }
}
