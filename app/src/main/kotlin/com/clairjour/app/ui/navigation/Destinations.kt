package com.clairjour.app.ui.navigation

object Destinations {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val JOURNAL = "journal"
    const val JOURNAL_EDITOR = "journal/editor"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val ADDICTION_DETAIL = "addiction/{addictionId}"
    const val ADDICTION_EDIT = "addiction/edit?addictionId={addictionId}"

    fun addictionDetail(id: String) = "addiction/$id"
    fun addictionEdit(id: String? = null) =
        if (id == null) "addiction/edit" else "addiction/edit?addictionId=$id"
}
