package com.example.jerseywooly.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import java.nio.file.Path
import java.time.Instant

// パスリスト
@Entity(tableName = "music_directory", indices = [Index(value = ["directory"], unique = true)])
data class MusicDirectory(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var directory: Path,
    var lastScannedAt: Instant
)

@Dao
interface MusicDirectoryDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(directory: MusicDirectory) : Long

    @Query("DELETE FROM music_directory")
    suspend fun deleteAll()

    @Query("SELECT * FROM music_directory")
    suspend fun getAllDirectories(): List<MusicDirectory>

    @Query("SELECT * FROM music_directory WHERE directory LIKE :dir || '%'")
    suspend fun getContainDirectories(dir : Path): List<MusicDirectory>

    @Query("DELETE FROM music_directory WHERE directory LIKE :dir || '%' AND lastScannedAt < :time")
    suspend fun deleteContainDirectoriesScannedBefore(dir: Path, time: Instant)

}
