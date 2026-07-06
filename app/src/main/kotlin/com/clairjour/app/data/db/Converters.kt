package com.clairjour.app.data.db

import androidx.room.TypeConverter
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class Converters {
    private val stringListSerializer = ListSerializer(String.serializer())

    @TypeConverter
    fun instantToLong(value: Instant?): Long? = value?.toEpochMilliseconds()

    @TypeConverter
    fun longToInstant(value: Long?): Instant? = value?.let { Instant.fromEpochMilliseconds(it) }

    @TypeConverter
    fun localDateToString(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun stringListToString(list: List<String>?): String =
        Json.encodeToString(stringListSerializer, list ?: emptyList())

    @TypeConverter
    fun stringToStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return runCatching { Json.decodeFromString(stringListSerializer, value) }
            .getOrDefault(emptyList())
    }
}
