package com.clairjour.app.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

object Streak {
    fun daysSince(startDate: Instant, now: Instant = Clock.System.now()): Int {
        val zone = TimeZone.currentSystemDefault()
        val startDay = startDate.toLocalDateTime(zone).date
        val today = now.toLocalDateTime(zone).date
        return startDay.daysUntil(today).coerceAtLeast(0)
    }

    fun daysBetween(a: LocalDate, b: LocalDate): Int = a.daysUntil(b)
}
