package com.clairjour.app.data.repository

import com.clairjour.app.data.db.AddictionDao
import com.clairjour.app.data.db.MilestoneDao
import com.clairjour.app.data.db.RelapseDao
import com.clairjour.app.data.db.RelapseEventEntity
import com.clairjour.app.domain.Streak
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID

class RelapseRepository(
    private val relapseDao: RelapseDao,
    private val milestoneDao: MilestoneDao,
    private val addictionDao: AddictionDao
) {
    fun observeFor(addictionId: String): Flow<List<RelapseEventEntity>> =
        relapseDao.observeFor(addictionId)

    suspend fun reportRelapse(addictionId: String, notes: String?, triggers: List<String>) {
        val addiction = addictionDao.getById(addictionId) ?: return
        val zone = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val today: LocalDate = now.toLocalDateTime(zone).date
        val previousStreak = Streak.daysSince(addiction.startDate, now)

        if (relapseDao.countForDate(addictionId, today) > 0) return

        relapseDao.insert(
            RelapseEventEntity(
                id = UUID.randomUUID().toString(),
                addictionId = addictionId,
                date = today,
                notes = notes,
                triggers = triggers,
                previousStreakDays = previousStreak
            )
        )
        milestoneDao.clearFor(addictionId)
        addictionDao.resetStart(addictionId, now)
    }
}
