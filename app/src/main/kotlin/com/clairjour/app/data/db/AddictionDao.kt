package com.clairjour.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
abstract class AddictionDao {
    @Query("SELECT * FROM addictions WHERE is_active = 1 ORDER BY is_primary DESC, created_at ASC")
    abstract fun observeActive(): Flow<List<AddictionEntity>>

    @Query("SELECT * FROM addictions WHERE is_active = 1 AND is_primary = 1 LIMIT 1")
    abstract fun observePrimary(): Flow<AddictionEntity?>

    @Query("SELECT * FROM addictions WHERE id = :id")
    abstract suspend fun getById(id: String): AddictionEntity?

    @Query("SELECT * FROM addictions WHERE id = :id")
    abstract fun observeById(id: String): Flow<AddictionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(entity: AddictionEntity)

    @Update
    abstract suspend fun update(entity: AddictionEntity)

    @Query("UPDATE addictions SET is_primary = 0")
    abstract suspend fun clearPrimary()

    @Query("UPDATE addictions SET is_primary = 1 WHERE id = :id")
    abstract suspend fun markPrimary(id: String)

    @Query("UPDATE addictions SET is_active = 0 WHERE id = :id")
    abstract suspend fun softDelete(id: String)

    @Query("UPDATE addictions SET start_date = :startDate WHERE id = :id")
    abstract suspend fun resetStart(id: String, startDate: Instant)

    @Transaction
    open suspend fun insertWithPrimaryHandling(entity: AddictionEntity) {
        if (entity.isPrimary) clearPrimary()
        insert(entity)
    }

    @Transaction
    open suspend fun updateWithPrimaryHandling(entity: AddictionEntity) {
        if (entity.isPrimary) clearPrimary()
        update(entity)
    }

    @Transaction
    open suspend fun setPrimary(id: String) {
        clearPrimary()
        markPrimary(id)
    }
}
