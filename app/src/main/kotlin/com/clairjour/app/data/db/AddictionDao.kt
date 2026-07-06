package com.clairjour.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface AddictionDao {
    @Query("SELECT * FROM addictions WHERE is_active = 1 ORDER BY is_primary DESC, created_at ASC")
    fun observeActive(): Flow<List<AddictionEntity>>

    @Query("SELECT * FROM addictions WHERE is_active = 1 AND is_primary = 1 LIMIT 1")
    fun observePrimary(): Flow<AddictionEntity?>

    @Query("SELECT * FROM addictions WHERE id = :id")
    suspend fun getById(id: String): AddictionEntity?

    @Query("SELECT * FROM addictions WHERE id = :id")
    fun observeById(id: String): Flow<AddictionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AddictionEntity)

    @Update
    suspend fun update(entity: AddictionEntity)

    @Query("UPDATE addictions SET is_primary = 0")
    suspend fun clearPrimary()

    @Query("UPDATE addictions SET is_primary = 1 WHERE id = :id")
    suspend fun markPrimary(id: String)

    @Query("UPDATE addictions SET is_active = 0 WHERE id = :id")
    suspend fun softDelete(id: String)

    @Query("UPDATE addictions SET start_date = :startDate WHERE id = :id")
    suspend fun resetStart(id: String, startDate: Instant)
}
