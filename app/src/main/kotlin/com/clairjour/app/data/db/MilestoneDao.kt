package com.clairjour.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MilestoneDao {
    @Query("SELECT * FROM milestones_reached WHERE addiction_id = :addictionId ORDER BY milestone_days ASC")
    fun observeFor(addictionId: String): Flow<List<MilestoneReachedEntity>>

    @Query("SELECT COUNT(*) FROM milestones_reached")
    fun countAll(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: MilestoneReachedEntity)

    @Query("UPDATE milestones_reached SET seen_by_user = 1 WHERE id = :id")
    suspend fun markSeen(id: String)

    @Query("DELETE FROM milestones_reached WHERE addiction_id = :addictionId")
    suspend fun clearFor(addictionId: String)
}
