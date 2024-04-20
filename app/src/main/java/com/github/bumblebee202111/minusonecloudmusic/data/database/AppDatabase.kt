package com.github.bumblebee202111.minusonecloudmusic.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.bumblebee202111.minusonecloudmusic.data.database.dao.SongDao
import com.github.bumblebee202111.minusonecloudmusic.data.database.dao.PlayerDao
import com.github.bumblebee202111.minusonecloudmusic.data.database.dao.PlaylistDao
import com.github.bumblebee202111.minusonecloudmusic.data.database.dao.RecentPlayDao
import com.github.bumblebee202111.minusonecloudmusic.data.database.dao.UserDao
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.LocalSongEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.RemoteSongEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.MyRecentMusicDataEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.PlayerPlaylistSongEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.PlaylistEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.UserDetailEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.UserPlaylistEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.UserProfileEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.view.GenericSongView
import dagger.hilt.android.qualifiers.ApplicationContext


@Database(
    entities = [
        UserProfileEntity::class,
        UserDetailEntity::class,
        PlaylistEntity::class,
        RemoteSongEntity::class,
        LocalSongEntity::class,
        UserPlaylistEntity::class,
        MyRecentMusicDataEntity::class,
        PlayerPlaylistSongEntity::class
    ],
    views = [GenericSongView::class],
    version = 17,
    exportSchema = false
)
@TypeConverters(com.github.bumblebee202111.minusonecloudmusic.data.database.TypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun recentPlayDao(): RecentPlayDao
    abstract fun songDao(): SongDao

    abstract fun playerDao(): PlayerDao

    companion object {
        private const val DATABASE_NAME = "mom-db"

        fun buildDatabase(@ApplicationContext context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}

