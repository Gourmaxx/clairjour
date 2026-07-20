package com.clairjour.app.data.backup

import android.content.ContentResolver
import android.net.Uri
import androidx.room.withTransaction
import com.clairjour.app.data.db.AddictionEntity
import com.clairjour.app.data.db.ClairjourDatabase
import com.clairjour.app.data.db.JournalEntryEntity
import com.clairjour.app.data.db.MilestoneReachedEntity
import com.clairjour.app.data.db.PledgeEntity
import com.clairjour.app.data.db.RelapseEventEntity
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json

/**
 * Backup format:
 *  * Modern (v0.2+): binary blob starting with BackupCrypto.MAGIC — AES-GCM encrypted JSON.
 *  * Legacy (v0.1): plaintext UTF-8 JSON — read but never written.
 *
 * A single [BackupData.version] field guards forward-compat inside the encrypted payload.
 */
class BackupRepository(
    private val db: ClairjourDatabase,
    private val contentResolver: ContentResolver
) {
    private val json = Json { prettyPrint = false; ignoreUnknownKeys = true }

    /**
     * Export the whole DB to [uri] as an encrypted blob using [passphrase].
     * Passphrase should be zeroed by the caller after use.
     */
    suspend fun export(uri: Uri, passphrase: CharArray) {
        val data = BackupData(
            version = CURRENT_BACKUP_VERSION,
            exportedAt = System.currentTimeMillis(),
            addictions = db.addictionDao().getAll().map { it.toBackup() },
            journalEntries = db.journalDao().getAll().map { it.toBackup() },
            pledges = db.pledgeDao().getAll().map { it.toBackup() },
            milestones = db.milestoneDao().getAll().map { it.toBackup() },
            relapseEvents = db.relapseDao().getAll().map { it.toBackup() }
        )
        val plaintext = json.encodeToString(BackupData.serializer(), data).toByteArray(Charsets.UTF_8)
        val blob = BackupCrypto.encrypt(plaintext, passphrase)
        contentResolver.openOutputStream(uri)?.use { it.write(blob) }
            ?: error("Cannot open output stream for $uri")
    }

    /**
     * Import a backup from [uri]. Detects encrypted vs legacy plaintext automatically.
     * The import runs inside a single Room transaction and is validated before any deletion.
     *
     * Throws:
     *  - [BackupBadPassphraseException] if [passphrase] is wrong on an encrypted backup.
     *  - [BackupCorruptException] on malformed content.
     *  - [BackupUnsupportedVersionException] if the payload version is unknown.
     *  - [BackupTooLargeException] if the file / row counts exceed safety caps.
     */
    suspend fun import(uri: Uri, passphrase: CharArray?) {
        val raw = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Cannot open input stream for $uri")
        if (raw.size > MAX_BACKUP_BYTES) {
            throw BackupTooLargeException("Backup exceeds ${MAX_BACKUP_BYTES / (1024 * 1024)} MB cap")
        }

        val jsonBytes: ByteArray = if (BackupCrypto.hasMagic(raw)) {
            requireNotNull(passphrase) { "Passphrase required for encrypted backup" }
            BackupCrypto.decrypt(raw, passphrase)
        } else {
            // Legacy plaintext JSON (pre-encryption). Kept for one-shot migration.
            raw
        }

        val data = try {
            json.decodeFromString(BackupData.serializer(), jsonBytes.toString(Charsets.UTF_8))
        } catch (e: Exception) {
            throw BackupCorruptException("Invalid backup JSON", e)
        }

        if (data.version > CURRENT_BACKUP_VERSION) {
            throw BackupUnsupportedVersionException("Unsupported backup version: ${data.version}")
        }
        enforceRowLimits(data)

        db.withTransaction {
            db.addictionDao().deleteAll()
            db.journalDao().deleteAll()
            db.pledgeDao().deleteAll()
            db.milestoneDao().deleteAll()
            db.relapseDao().deleteAll()

            data.addictions.map { it.toEntity() }.also { db.addictionDao().insertAll(it) }
            data.journalEntries.map { it.toEntity() }.also { db.journalDao().insertAll(it) }
            data.pledges.map { it.toEntity() }.also { db.pledgeDao().insertAll(it) }
            data.milestones.map { it.toEntity() }.also { db.milestoneDao().insertAll(it) }
            data.relapseEvents.map { it.toEntity() }.also { db.relapseDao().insertAll(it) }
        }
    }

    private fun enforceRowLimits(data: BackupData) {
        val counts = listOf(
            data.addictions.size,
            data.journalEntries.size,
            data.pledges.size,
            data.milestones.size,
            data.relapseEvents.size
        )
        if (counts.any { it > MAX_ROWS_PER_TABLE }) {
            throw BackupTooLargeException("A table exceeds $MAX_ROWS_PER_TABLE rows")
        }
    }

    // --- Entity → Backup ---

    private fun AddictionEntity.toBackup() = AddictionBackup(
        id, name, type,
        startDate.toEpochMilliseconds(),
        costPerDay, unitPerDay, unitLabel, colorSeed, isPrimary, isActive,
        createdAt.toEpochMilliseconds()
    )

    private fun JournalEntryEntity.toBackup() = JournalBackup(
        id, date.toString(), mood, notes, triggers, gratitude, hadCravings,
        createdAt.toEpochMilliseconds(), updatedAt.toEpochMilliseconds()
    )

    private fun PledgeEntity.toBackup() = PledgeBackup(
        id, addictionId, date.toString(), timestamp.toEpochMilliseconds()
    )

    private fun MilestoneReachedEntity.toBackup() = MilestoneBackup(
        id, addictionId, milestoneDays, reachedAt.toEpochMilliseconds(), seenByUser
    )

    private fun RelapseEventEntity.toBackup() = RelapseBackup(
        id, addictionId, date.toString(), notes, triggers, previousStreakDays
    )

    // --- Backup → Entity ---

    private fun AddictionBackup.toEntity() = AddictionEntity(
        id, name, type,
        Instant.fromEpochMilliseconds(startDateMs),
        costPerDay, unitPerDay, unitLabel, colorSeed, isPrimary, isActive,
        Instant.fromEpochMilliseconds(createdAtMs)
    )

    private fun JournalBackup.toEntity() = JournalEntryEntity(
        id, LocalDate.parse(date), mood, notes, triggers, gratitude, hadCravings,
        Instant.fromEpochMilliseconds(createdAtMs),
        Instant.fromEpochMilliseconds(updatedAtMs)
    )

    private fun PledgeBackup.toEntity() = PledgeEntity(
        id, addictionId, LocalDate.parse(date), Instant.fromEpochMilliseconds(timestampMs)
    )

    private fun MilestoneBackup.toEntity() = MilestoneReachedEntity(
        id, addictionId, milestoneDays, Instant.fromEpochMilliseconds(reachedAtMs), seenByUser
    )

    private fun RelapseBackup.toEntity() = RelapseEventEntity(
        id, addictionId, LocalDate.parse(date), notes, triggers, previousStreakDays
    )

    companion object {
        const val CURRENT_BACKUP_VERSION = 1
        private const val MAX_BACKUP_BYTES = 10L * 1024L * 1024L
        private const val MAX_ROWS_PER_TABLE = 50_000
    }
}

class BackupTooLargeException(message: String) : Exception(message)

// AddictionDao needs insertAll — add it to the DAO separately
private suspend fun com.clairjour.app.data.db.AddictionDao.insertAll(entities: List<AddictionEntity>) {
    entities.forEach { insert(it) }
}
