package com.clairjour.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RelapseDao {
    @Query("SELECT * FROM relapse_events WHERE addiction_id = :addictionId ORDER BY date DESC")
    fun observeFor(addictionId: String): Flow<List<RelapseEventEntity>>

    @Insert
    suspend fun insert(entity: RelapseEventEntity)
}
