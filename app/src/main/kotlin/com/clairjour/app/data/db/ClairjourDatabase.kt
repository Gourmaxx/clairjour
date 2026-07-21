package com.clairjour.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
         * Migration strategy: v0.1.0 is pre-release, so we accept destructive migrations.
         * When SQLCipher is introduced on top of an existing plaintext DB, Room will fail
         * to open the file (bad magic) and the fallback wipes local data. Users are asked
         * to re-onboard. Encrypted backups (see BackupCrypto) allow restoring history.
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
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
