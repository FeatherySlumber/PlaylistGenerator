package com.example.jerseywooly.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.nio.file.Path

// フォルダの走査対象、ファイルの選択対象
@Entity(tableName = "selected_directory", indices = [Index(value = ["directory"], unique = true)])
data class SelectedDirectory(
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0,
    var directory: Path
)

@Dao
interface SelectedDirectoryDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg directory: SelectedDirectory)

    @Delete
    suspend fun deleteDirectory(vararg directory: SelectedDirectory)

    @Query("SELECT * FROM selected_directory")
    suspend fun getAllDirectory(): List<SelectedDirectory>

    @Query("SELECT * FROM selected_directory")
    fun getAllDirectoryStream(): Flow<List<SelectedDirectory>>

    @Query("DELETE FROM selected_directory WHERE directory LIKE :dir || '%'")
    suspend fun deleteContainDirectory(dir : Path)
}
