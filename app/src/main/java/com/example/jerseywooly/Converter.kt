package com.example.jerseywooly

import androidx.room.TypeConverter
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import kotlin.math.ceil


class PathConverters {
    @TypeConverter
    fun fromPath(value: Path?): String? {
        return value?.toAbsolutePath()?.toString()
    }

    @TypeConverter
    fun toPath(value: String?) : Path? {
        return value?.let{
            Paths.get(it)
        }
    }
}

class InstantConverters {
    @TypeConverter
    fun fromInstant(value: Instant?): Long? {
        return value?.epochSecond
    }

    @TypeConverter
    fun toInstant(value: Long?) : Instant? {
        return value?.let{
            Instant.ofEpochSecond(it)
        }
    }
}

fun milliTimeToString(value : Int) : String{
    val minute = value / (60 * 1000)
    val second = ceil((value - minute * (60 * 1000)).toDouble() / 1000).toInt()
    val minuteS = minute.toString().padStart(2, '0')
    val secondS = second.toString().padStart(2, '0')
    return "$minuteS:$secondS"
}

