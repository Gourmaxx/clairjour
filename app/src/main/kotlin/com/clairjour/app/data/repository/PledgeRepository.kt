package com.clairjour.app.data.repository

import com.clairjour.app.data.db.PledgeDao
import com.clairjour.app.data.db.PledgeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID

class PledgeRepository(private val dao: PledgeDao) {

    fun observeFor(addictionId: String, date: LocalDate): Flow<PledgeEntity?> =
        dao.observeFor(addictionId, date)

    fun countFor(addictionId: String): Flow<Int> = dao.countFor(addictionId)

    suspend fun pledge(addictionId: String, date: LocalDate) {
        dao.insert(
            PledgeEntity(
                id = UUID.randomUUID().toString(),
                addictionId = addictionId,
                date = date,
                timestamp = Clock.System.now()
            )
        )
    }

    fun today(): LocalDate =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
}
