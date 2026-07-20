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

    /**
     * Reports a relapse: inserts a RelapseEvent, clears milestones, and resets the streak.
     * Returns the previous start date so callers can restore it if the user undoes the action.
     */
    suspend fun reportRelapse(
        addictionId: String,
        notes: String?,
        triggers: List<String>
    ): RelapseSnapshot? {
        val addiction = addictionDao.getById(addictionId) ?: return null
        val zone = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val today: LocalDate = now.toLocalDateTime(zone).date
        val previousStreak = Streak.daysSince(addiction.startDate, now)
        val previousStart = addiction.startDate

        if (relapseDao.countForDate(addictionId, today) > 0) return null

        val relapseId = UUID.randomUUID().toString()
        relapseDao.insert(
            RelapseEventEntity(
                id = relapseId,
                addictionId = addictionId,
                date = today,
                notes = notes,
                triggers = triggers,
                previousStreakDays = previousStreak
            )
        )
        milestoneDao.clearFor(addictionId)
        addictionDao.resetStart(addictionId, now)
        return RelapseSnapshot(relapseId = relapseId, addictionId = addictionId, previousStart = previousStart)
    }

    /**
     * Undoes a previous relapse: deletes the RelapseEvent and restores the addiction start date.
     * Milestones cleared by reportRelapse are NOT restored (they will be recomputed by the domain).
     */
    suspend fun undoRelapse(snapshot: RelapseSnapshot) {
        relapseDao.deleteById(snapshot.relapseId)
        addictionDao.resetStart(snapshot.addictionId, snapshot.previousStart)
    }
}

data class RelapseSnapshot(
    val relapseId: String,
    val addictionId: String,
    val previousStart: kotlinx.datetime.Instant
)
