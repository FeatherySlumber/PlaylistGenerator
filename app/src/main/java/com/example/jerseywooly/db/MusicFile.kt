package com.example.jerseywooly.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import com.example.jerseywooly.milliTimeToString
import kotlinx.coroutines.flow.Flow

// 音楽ファイル
@Entity(tableName = "music_file",
    foreignKeys = [ForeignKey(entity = MusicDirectory::class, parentColumns = ["id"], childColumns = ["directory_id"], onDelete = ForeignKey.CASCADE)])
data class MusicFile(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var title: String,
    var artist: String,
    var length: Int,
    var fileName: String,
    @ColumnInfo(name = "directory_id", index = true) var directoryId: Long
){
    val playTimeString : String
        get() = milliTimeToString(length)
}

@Dao
interface MusicFileDao{
    @Insert
    suspend fun insert(vararg file: MusicFile)

    @Query("DELETE FROM music_file WHERE directory_id = :dId")
    suspend fun deleteDirectoryId(dId: Int)

    @Query("DELETE FROM music_file")
    suspend fun deleteAll()

    @Query("SELECT * FROM music_file WHERE directory_id = :dId")
    suspend fun selectInDirectory(dId: Long): List<MusicFile>

    @Query("SELECT * FROM music_file WHERE directory_id IN (:dIds)")
    suspend fun selectInDirectories(dIds: List<Long>): List<MusicFile>

    @Query("SELECT * FROM music_file WHERE directory_id IN (:dIds) AND length <= :duration")
    suspend fun selectInDirectoriesShorterThan(dIds: List<Long>, duration: Int): List<MusicFile>

    @Query("SELECT COUNT(*) FROM music_file")
    suspend fun countInDirectories(): Int

    // テーブル内容変更取得のため使用
    @Query("SELECT MAX(id) FROM music_file")
    fun maxId() : Flow<Int>
}