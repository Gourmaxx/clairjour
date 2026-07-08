package com.clairjour.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        AddictionEntity::class,
        JournalEntryEntity::class,
        PledgeEntity::class,
        MilestoneReachedEntity::class,
        RelapseEventEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ClairjourDatabase : RoomDatabase() {
    abstract fun addictionDao(): AddictionDao
    abstract fun journalDao(): JournalDao
    abstract fun pledgeDao(): PledgeDao
    abstract fun milestoneDao(): MilestoneDao
    abstract fun relapseDao(): RelapseDao

    companion object {
        private const val DB_NAME = "clairjour.db"

        @Volatile private var instance: ClairjourDatabase? = null

        fun get(context: Context): ClairjourDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ClairjourDatabase::class.java,
                    DB_NAME
                ).fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
    }
}
