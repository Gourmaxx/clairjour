package com.clairjour.app.ui.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clairjour.app.data.prefs.AppLanguage
import com.clairjour.app.data.prefs.SettingsRepository
import com.clairjour.app.data.repository.AddictionRepository
import com.clairjour.app.domain.AddictionType
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class OnboardingViewModel(
    private val settings: SettingsRepository,
    private val addictions: AddictionRepository
) : ViewModel() {

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { settings.setLanguage(language) }
    }

    fun finish(
        language: AppLanguage,
        addictionType: AddictionType,
        addictionName: String,
        startDate: Instant,
        costPerDay: Double?,
        unitPerDay: Double?,
        unitLabel: String?,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            settings.setLanguage(language)
            addictions.create(
                name = addictionName.ifBlank { "?" },
                type = addictionType,
                startDate = startDate,
                costPerDay = costPerDay,
                unitPerDay = unitPerDay,
                unitLabel = unitLabel,
                isPrimary = true
            )
            settings.setOnboardingDone(true)
            onDone()
        }
    }

    companion object {
        fun defaultStart(): Instant = Clock.System.now()
    }
}
