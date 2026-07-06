package com.clairjour.app.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "date") val date: LocalDate,
    @ColumnInfo(name = "mood") val mood: Int,
    @ColumnInfo(name = "notes") val notes: String,
    @ColumnInfo(name = "triggers") val triggers: List<String>,
    @ColumnInfo(name = "gratitude") val gratitude: String?,
    @ColumnInfo(name = "had_cravings") val hadCravings: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant
)
