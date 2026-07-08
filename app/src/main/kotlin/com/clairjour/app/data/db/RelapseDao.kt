package com.clairjour.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface RelapseDao {
    @Query("SELECT * FROM relapse_events WHERE addiction_id = :addictionId ORDER BY date DESC")
    fun observeFor(addictionId: String): Flow<List<RelapseEventEntity>>

    @Query("SELECT COUNT(*) FROM relapse_events WHERE addiction_id = :addictionId AND date = :date")
    suspend fun countForDate(addictionId: String, date: LocalDate): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: RelapseEventEntity)
}
