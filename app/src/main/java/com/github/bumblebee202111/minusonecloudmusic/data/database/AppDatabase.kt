package com.github.bumblebee202111.minusonecloudmusic.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.bumblebee202111.minusonecloudmusic.data.database.dao.MusicInfoDao
import com.github.bumblebee202111.minusonecloudmusic.data.database.dao.PlaylistDao
import com.github.bumblebee202111.minusonecloudmusic.data.database.dao.RecentPlayDao
import com.github.bumblebee202111.minusonecloudmusic.data.database.dao.UserDao
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.MusicInfoEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.MyRecentMusicDataEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.PlaylistEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.UserDetailEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.UserPlaylistEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.UserProfileEntity
import dagger.hilt.android.qualifiers.ApplicationContext


@Database(
    entities = [UserProfileEntity::class, UserDetailEntity::class, PlaylistEntity::class, MusicInfoEntity::class, UserPlaylistEntity::class, MyRecentMusicDataEntity::class],
    version = 11,
    exportSchema = false
)
@TypeConverters(com.github.bumblebee202111.minusonecloudmusic.data.database.TypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun recentPlayDao(): RecentPlayDao
    abstract fun musicInfoDao(): MusicInfoDao

    companion object {
        private const val DATABASE_NAME = "mom-db"

        fun buildDatabase(@ApplicationContext context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}

