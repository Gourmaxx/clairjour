package com.clairjour.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MilestonesTest {

    @Test
    fun `reached returns all milestones up to given days`() {
        val reached = Milestones.reached(30)
        assertEquals(listOf(1, 3, 7, 14, 30), reached.map { it.days })
    }

    @Test
    fun `reached returns empty when zero days`() {
        assertEquals(emptyList<Milestone>(), Milestones.reached(0))
    }

    @Test
    fun `next returns first milestone strictly greater than days`() {
        assertEquals(7, Milestones.next(3)?.days)
        assertEquals(30, Milestones.next(14)?.days)
    }

    @Test
    fun `next returns null when beyond all milestones`() {
        assertNull(Milestones.next(9999))
    }

    @Test
    fun `progressToNext is zero right after a milestone`() {
        val progress = Milestones.progressToNext(30)
        assertEquals(0f, progress, 0.001f)
    }

    @Test
    fun `progressToNext is close to one right before next milestone`() {
        val progress = Milestones.progressToNext(29)
        assert(progress in 0.9f..1.0f)
    }
}
