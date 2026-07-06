package com.clairjour.app.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(
    tableName = "milestones_reached",
    indices = [Index(value = ["addiction_id", "milestone_days"], unique = true)]
)
data class MilestoneReachedEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "addiction_id") val addictionId: String,
    @ColumnInfo(name = "milestone_days") val milestoneDays: Int,
    @ColumnInfo(name = "reached_at") val reachedAt: Instant,
    @ColumnInfo(name = "seen_by_user") val seenByUser: Boolean
)
