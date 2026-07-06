package com.clairjour.app.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(tableName = "relapse_events")
data class RelapseEventEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "addiction_id") val addictionId: String,
    @ColumnInfo(name = "date") val date: LocalDate,
    @ColumnInfo(name = "notes") val notes: String?,
    @ColumnInfo(name = "triggers") val triggers: List<String>,
    @ColumnInfo(name = "previous_streak_days") val previousStreakDays: Int
)
