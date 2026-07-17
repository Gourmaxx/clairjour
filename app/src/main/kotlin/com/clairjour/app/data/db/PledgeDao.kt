package com.clairjour.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface PledgeDao {
    @Query("SELECT * FROM pledges WHERE addiction_id = :addictionId AND date = :date LIMIT 1")
    fun observeFor(addictionId: String, date: LocalDate): Flow<PledgeEntity?>

    @Query("SELECT COUNT(*) FROM pledges WHERE addiction_id = :addictionId")
    fun countFor(addictionId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: PledgeEntity)

    @Query("SELECT * FROM pledges")
    suspend fun getAll(): List<PledgeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<PledgeEntity>)

    @Query("DELETE FROM pledges")
    suspend fun deleteAll()
}
