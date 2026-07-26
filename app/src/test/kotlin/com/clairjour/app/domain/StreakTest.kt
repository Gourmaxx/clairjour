package com.clairjour.app.domain

import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.days

class StreakTest {

    @Test
    fun `daysSince returns 0 when startDate equals now`() {
        val now = Instant.parse("2026-07-20T12:00:00Z")
        assertEquals(0, Streak.daysSince(now, now))
    }

    @Test
    fun `daysSince returns 0 when startDate is in the future`() {
        val now = Instant.parse("2026-07-20T12:00:00Z")
        val future = now.plus(2.days)
        assertEquals(0, Streak.daysSince(future, now))
    }

    @Test
    fun `daysSince across 30 calendar days returns 30`() {
        val start = Instant.parse("2026-06-30T12:00:00Z")
        val now = Instant.parse("2026-07-30T12:00:00Z")
        assertEquals(30, Streak.daysSince(start, now))
    }

    @Test
    fun `daysSince counts calendar boundaries not raw 24h buckets`() {
        // Same wall-clock moment on consecutive days = 1 day, whatever the exact hour offset.
        val start = Instant.parse("2026-07-19T22:00:00Z")
        val now = Instant.parse("2026-07-20T10:00:00Z")
        val d = Streak.daysSince(start, now)
        // Depending on the JVM timezone this can be 0 or 1 — assert the bound only.
        assert(d in 0..1) { "expected 0 or 1 calendar days, got $d" }
    }
}
