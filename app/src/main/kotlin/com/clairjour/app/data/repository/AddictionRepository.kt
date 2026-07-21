package com.clairjour.app.data.repository

import com.clairjour.app.data.db.AddictionDao
import com.clairjour.app.data.db.AddictionEntity
import com.clairjour.app.domain.AddictionType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID

class AddictionRepository(private val dao: AddictionDao) {

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
                createdAt = Clock.System.now(),
                personalReasons = personalReasons
            )
        )
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
