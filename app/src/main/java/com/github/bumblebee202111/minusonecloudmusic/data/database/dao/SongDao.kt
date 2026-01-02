package com.github.bumblebee202111.minusonecloudmusic.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.LocalSongEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.RemoteSongEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.SongPrivilegePartialEntity

@Dao
interface SongDao {

    @Insert(entity = RemoteSongEntity::class, onConflict = OnConflictStrategy.REPLACE)
    fun insertRemoteSongs(songs: List<RemoteSongEntity>)

    @Insert(entity = LocalSongEntity::class, onConflict = OnConflictStrategy.REPLACE)
    fun insertLocalSongs(songs: List<LocalSongEntity>)

    @Query("SELECT * FROM LocalSongEntity WHERE id = :id")
    suspend fun localSong(id: Long): LocalSongEntity

    @Query("SELECT * FROM LocalSongEntity WHERE id IN( :ids)")
    suspend fun localSongs(ids: List<Long>): List<LocalSongEntity>

    @Query("SELECT * FROM RemoteSongEntity WHERE id IN (:id)")
    suspend fun remoteSong(id: Long): RemoteSongEntity

    @Query("SELECT * FROM RemoteSongEntity WHERE id IN( :ids)")
    suspend fun remoteSongs(ids: List<Long>): List<RemoteSongEntity>

    @Update(entity = RemoteSongEntity::class)
    suspend fun upsertPrivileges(userSongs: List<SongPrivilegePartialEntity>)

}