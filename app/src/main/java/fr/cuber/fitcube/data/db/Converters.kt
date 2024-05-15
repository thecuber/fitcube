package fr.cuber.fitcube.data.db

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }

    @TypeConverter
    fun listFromInt(value : List<Int>) = Json.encodeToString(value)

    @TypeConverter
    fun listToInt(value: String) = Json.decodeFromString<List<Int>>(value)

    @TypeConverter
    fun listFromDouble(value : List<Double>) = Json.encodeToString(value)

    @TypeConverter
    fun listToDouble(value: String) = Json.decodeFromString<List<Double>>(value)

    @TypeConverter
    fun doubleListFromInt(value : List<List<Double>>) = Json.encodeToString(value)

    @TypeConverter
    fun doubleListToInt(value: String) = Json.decodeFromString<List<List<Double>>>(value)

    @TypeConverter
    fun mapListFromInt(value : Map<Int, List<Double>>) = Json.encodeToString(value)

    @TypeConverter
    fun mapListToInt(value: String) = Json.decodeFromString<Map<Int, List<Double>>>(value)



}