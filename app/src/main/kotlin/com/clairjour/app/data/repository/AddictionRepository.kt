package com.clairjour.app.data.repository

import com.clairjour.app.data.db.AddictionDao
import com.clairjour.app.data.db.AddictionEntity
import com.clairjour.app.data.db.MilestoneDao
import com.clairjour.app.data.db.MilestoneReachedEntity
import com.clairjour.app.domain.AddictionType
import com.clairjour.app.domain.Milestones
import com.clairjour.app.domain.Streak
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID

class AddictionRepository(
    private val dao: AddictionDao,
    private val milestoneDao: MilestoneDao? = null
) {

    fun observeActive(): Flow<List<AddictionEntity>> = dao.observeActive()
    fun observePrimary(): Flow<AddictionEntity?> = dao.observePrimary()
    fun observeById(id: String): Flow<AddictionEntity?> = dao.observeById(id)

    suspend fun getById(id: String): AddictionEntity? = dao.getById(id)

    suspend fun create(
        name: String,
        type: AddictionType,
        startDate: Instant,
        costPerDay: Double?,
        unitPerDay: Double?,
        unitLabel: String?,
        isPrimary: Boolean,
        personalReasons: List<String> = emptyList()
    ): String {
        val id = UUID.randomUUID().toString()
        val now = Clock.System.now()
        if (isPrimary) dao.clearPrimary()
        dao.insert(
            AddictionEntity(
                id = id,
                name = name.ifBlank { "?" },
                type = type.name,
                startDate = startDate,
                costPerDay = costPerDay,
                unitPerDay = unitPerDay,
                unitLabel = unitLabel,
                colorSeed = type.ordinal * 37,
                isPrimary = isPrimary,
                isActive = true,
                createdAt = now,
                personalReasons = personalReasons
            )
        )
        // Backfill: if the user pre-dated their sobriety, milestones already reached at
        // creation are inserted as *already seen* so we don't flash a celebration cascade
        // on the first Home render. Only makes sense when milestoneDao is wired in.
        val elapsedAtStart = Streak.daysSince(startDate, now)
        if (milestoneDao != null && elapsedAtStart > 0) {
            Milestones.reached(elapsedAtStart).forEach { m ->
                milestoneDao.insert(
                    MilestoneReachedEntity(
                        id = "${id}_${m.days}",
                        addictionId = id,
                        milestoneDays = m.days,
                        reachedAt = now,
                        seenByUser = true
                    )
                )
            }
        }
        return id
    }

    suspend fun update(entity: AddictionEntity) {
        if (entity.isPrimary) dao.clearPrimary()
        dao.update(entity)
    }

    suspend fun markPrimary(id: String) {
        dao.clearPrimary()
        dao.markPrimary(id)
    }

    suspend fun softDelete(id: String) = dao.softDelete(id)

    suspend fun resetStart(id: String, newStart: Instant) = dao.resetStart(id, newStart)
}
