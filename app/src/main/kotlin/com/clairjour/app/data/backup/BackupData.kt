package com.clairjour.app.data.backup

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val version: Int = 1,
    val exportedAt: Long,
    val addictions: List<AddictionBackup> = emptyList(),
    val journalEntries: List<JournalBackup> = emptyList(),
    val pledges: List<PledgeBackup> = emptyList(),
    val milestones: List<MilestoneBackup> = emptyList(),
    val relapseEvents: List<RelapseBackup> = emptyList()
)

@Serializable
data class AddictionBackup(
    val id: String,
    val name: String,
    val type: String,
    val startDateMs: Long,
    val costPerDay: Double?,
    val unitPerDay: Double?,
    val unitLabel: String?,
    val colorSeed: Int,
    val isPrimary: Boolean,
    val isActive: Boolean,
    val createdAtMs: Long
)

@Serializable
data class JournalBackup(
    val id: String,
    val date: String,
    val mood: Int,
    val notes: String,
    val triggers: List<String>,
    val gratitude: String?,
    val hadCravings: Boolean,
    val createdAtMs: Long,
    val updatedAtMs: Long
)

@Serializable
data class PledgeBackup(
    val id: String,
    val addictionId: String,
    val date: String,
    val timestampMs: Long
)

@Serializable
data class MilestoneBackup(
    val id: String,
    val addictionId: String,
    val milestoneDays: Int,
    val reachedAtMs: Long,
    val seenByUser: Boolean
)

@Serializable
data class RelapseBackup(
    val id: String,
    val addictionId: String,
    val date: String,
    val notes: String?,
    val triggers: List<String>,
    val previousStreakDays: Int
)
