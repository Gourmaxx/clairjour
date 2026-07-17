package com.clairjour.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.clairjour.app.data.db.AddictionEntity
import com.clairjour.app.data.db.ClairjourDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddictionDaoTest {

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

    private fun addiction(id: String = "a1", isPrimary: Boolean = true) = AddictionEntity(
        id = id,
        name = "Test",
        type = "alcohol",
        startDate = Clock.System.now(),
        costPerDay = null,
        unitPerDay = null,
        unitLabel = null,
        colorSeed = 0,
        isPrimary = isPrimary,
        isActive = true,
        createdAt = Clock.System.now()
    )

    @Test
    fun insertAndObserveActive() = runTest {
        db.addictionDao().insert(addiction())
        val list = db.addictionDao().observeActive().first()
        assertEquals(1, list.size)
        assertEquals("a1", list[0].id)
    }

    @Test
    fun softDeleteHidesFromActive() = runTest {
        db.addictionDao().insert(addiction())
        db.addictionDao().softDelete("a1")
        val list = db.addictionDao().observeActive().first()
        assertEquals(0, list.size)
    }

    @Test
    fun getByIdReturnsNull_whenNotFound() = runTest {
        assertNull(db.addictionDao().getById("nonexistent"))
    }

    @Test
    fun markPrimarySetsOnlyOneAsPrimary() = runTest {
        db.addictionDao().insert(addiction("a1", isPrimary = true))
        db.addictionDao().insert(addiction("a2", isPrimary = false))
        db.addictionDao().clearPrimary()
        db.addictionDao().markPrimary("a2")
        val list = db.addictionDao().observeActive().first()
        val primary = list.firstOrNull { it.isPrimary }
        assertEquals("a2", primary?.id)
    }

    @Test
    fun deleteAllClearsTable() = runTest {
        db.addictionDao().insert(addiction("a1"))
        db.addictionDao().insert(addiction("a2"))
        db.addictionDao().deleteAll()
        assertEquals(0, db.addictionDao().getAll().size)
    }
}
