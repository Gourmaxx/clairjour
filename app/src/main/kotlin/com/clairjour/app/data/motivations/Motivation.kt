package com.clairjour.app.data.motivations

import kotlinx.serialization.Serializable

@Serializable
data class Motivation(
    val id: Int,
    val fr: String,
    val en: String
) {
    fun textFor(languageTag: String): String =
        if (languageTag.startsWith("fr")) fr else en
}
