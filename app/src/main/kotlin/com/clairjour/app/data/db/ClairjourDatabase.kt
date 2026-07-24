package com.clairjour.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.clairjour.app.security.DatabasePassphraseProvider
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(
    entities = [
        AddictionEntity::class,
        JournalEntryEntity::class,
        PledgeEntity::class,
        MilestoneReachedEntity::class,
        RelapseEventEntity::class
    ],
    version = 2,
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
        @Volatile private var nativeLibsLoaded = false

        /**
         * Loads the SQLCipher native lib exactly once per process.
         * Must run before opening any encrypted database.
         */
        private fun ensureNativeLibs() {
            if (nativeLibsLoaded) return
            synchronized(ClairjourDatabase::class.java) {
                if (!nativeLibsLoaded) {
                    System.loadLibrary("sqlcipher")
                    nativeLibsLoaded = true
                }
            }
        }

        /**
         * v1 → v2 adds the `personal_reasons` column on `addictions`. Preserves user data.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE addictions ADD COLUMN personal_reasons TEXT NOT NULL DEFAULT '[]'"
                )
            }
        }

        /**
         * Migration strategy:
         *  - v1 → v2 : real ALTER, no data loss.
         *  - Legacy plaintext DB opened with SQLCipher key : falls back to destructive
         *    (bad-magic on read); users must re-onboard OR restore from an encrypted backup.
         */
        fun get(context: Context): ClairjourDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context).also { instance = it }
            }

        private fun build(context: Context): ClairjourDatabase {
            ensureNativeLibs()
            val passphrase = DatabasePassphraseProvider(context).getOrCreatePassphrase()
            val factory = SupportOpenHelperFactory(passphrase)
            return Room.databaseBuilder(
                context.applicationContext,
                ClairjourDatabase::class.java,
                DB_NAME
            )
                .openHelperFactory(factory)
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
