package com.clairjour.app.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "pledges",
    indices = [Index(value = ["addiction_id", "date"], unique = true)]
)
data class PledgeEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "addiction_id") val addictionId: String,
    @ColumnInfo(name = "date") val date: LocalDate,
    @ColumnInfo(name = "timestamp") val timestamp: Instant
)
