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

    @Query("DELETE FROM relapse_events WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM relapse_events")
    suspend fun getAll(): List<RelapseEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<RelapseEventEntity>)

    @Query("DELETE FROM relapse_events")
    suspend fun deleteAll()
}
