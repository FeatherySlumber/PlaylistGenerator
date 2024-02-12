package com.example.jerseywooly.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.jerseywooly.InstantConverters
import com.example.jerseywooly.PathConverters

@Database(entities = [SelectedDirectory::class, MusicDirectory::class, MusicFile::class], version = 5, exportSchema = false)
@TypeConverters(PathConverters::class, InstantConverters::class)
abstract class AppDatabase : RoomDatabase(){
    abstract fun selectedDirectoryDao() : SelectedDirectoryDao
    abstract fun musicDirectoryDao() : MusicDirectoryDao
    abstract fun musicFileDao() : MusicFileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also{ INSTANCE = it }
            }
        }
    }
}
