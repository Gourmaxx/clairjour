package com.clairjour.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.clairjour.app.data.db.ClairjourDatabase
import com.clairjour.app.data.db.MilestoneReachedEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MilestoneDaoTest {

    private lateinit var db: ClairjourDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ClairjourDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun teardown() { db.close() }

    private fun milestone(id: String, days: Int, seen: Boolean = false) = MilestoneReachedEntity(
        id = id,
        addictionId = "addiction1",
        milestoneDays = days,
        reachedAt = Clock.System.now(),
        seenByUser = seen
    )

    @Test
    fun insertAndObserve() = runTest {
        db.milestoneDao().insert(milestone("m1", 1))
        db.milestoneDao().insert(milestone("m7", 7))
        val list = db.milestoneDao().observeFor("addiction1").first()
        assertEquals(2, list.size)
        assertEquals(1, list[0].milestoneDays)
        assertEquals(7, list[1].milestoneDays)
    }

    @Test
    fun insertIgnoredOnConflict() = runTest {
        db.milestoneDao().insert(milestone("m1", 1, seen = false))
        db.milestoneDao().insert(milestone("m1", 1, seen = true)) // should be ignored
        val list = db.milestoneDao().getAll()
        assertEquals(1, list.size)
        assertFalse(list[0].seenByUser)
    }

    @Test
    fun markSeenUpdatesFlag() = runTest {
        db.milestoneDao().insert(milestone("m30", 30, seen = false))
        db.milestoneDao().markSeen("m30")
        val list = db.milestoneDao().getAll()
        assertTrue(list[0].seenByUser)
    }

    @Test
    fun clearForRemovesOnlyTargetAddiction() = runTest {
        db.milestoneDao().insert(milestone("m1", 1))
        val other = MilestoneReachedEntity(
            id = "other_m1",
            addictionId = "addiction2",
            milestoneDays = 1,
            reachedAt = Clock.System.now(),
            seenByUser = false
        )
        db.milestoneDao().insert(other)
        db.milestoneDao().clearFor("addiction1")
        val remaining = db.milestoneDao().getAll()
        assertEquals(1, remaining.size)
        assertEquals("addiction2", remaining[0].addictionId)
    }

    @Test
    fun deleteAllClearsTable() = runTest {
        db.milestoneDao().insert(milestone("m1", 1))
        db.milestoneDao().deleteAll()
        assertEquals(0, db.milestoneDao().getAll().size)
    }
}
