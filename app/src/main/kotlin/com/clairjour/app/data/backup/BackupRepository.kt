package com.clairjour.app.data.backup

import android.content.ContentResolver
import android.net.Uri
import com.clairjour.app.data.db.AddictionEntity
import com.clairjour.app.data.db.ClairjourDatabase
import com.clairjour.app.data.db.JournalEntryEntity
import com.clairjour.app.data.db.MilestoneReachedEntity
import com.clairjour.app.data.db.PledgeEntity
import com.clairjour.app.data.db.RelapseEventEntity
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json

class BackupRepository(
    private val db: ClairjourDatabase,
    private val contentResolver: ContentResolver
) {
    private val json = Json { prettyPrint = false; ignoreUnknownKeys = true }

    suspend fun export(uri: Uri) {
        val data = BackupData(
            exportedAt = System.currentTimeMillis(),
            addictions = db.addictionDao().getAll().map { it.toBackup() },
            journalEntries = db.journalDao().getAll().map { it.toBackup() },
            pledges = db.pledgeDao().getAll().map { it.toBackup() },
            milestones = db.milestoneDao().getAll().map { it.toBackup() },
            relapseEvents = db.relapseDao().getAll().map { it.toBackup() }
        )
        contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
            writer.write(json.encodeToString(BackupData.serializer(), data))
        } ?: error("Cannot open output stream for $uri")
    }

    suspend fun import(uri: Uri) {
        val raw = contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: error("Cannot open input stream for $uri")
        val data = json.decodeFromString(BackupData.serializer(), raw)

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
}

// AddictionDao needs insertAll — add it to the DAO separately
private suspend fun com.clairjour.app.data.db.AddictionDao.insertAll(entities: List<AddictionEntity>) {
    entities.forEach { insert(it) }
}
