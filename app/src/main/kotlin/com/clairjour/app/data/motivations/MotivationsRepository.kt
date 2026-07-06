package com.clairjour.app.data.motivations

import android.content.Context
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class MotivationsRepository(private val context: Context) {

    private val cached: List<Motivation> by lazy {
        val json = context.assets.open("motivations.json").bufferedReader().use { it.readText() }
        Json.decodeFromString(ListSerializer(Motivation.serializer()), json)
    }

    fun ofDay(): Motivation {
        val zone = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(zone).date
        val index = (today.toEpochDays().toLong() % cached.size).toInt().let {
            if (it < 0) it + cached.size else it
        }
        return cached[index]
    }

    fun all(): List<Motivation> = cached
}
