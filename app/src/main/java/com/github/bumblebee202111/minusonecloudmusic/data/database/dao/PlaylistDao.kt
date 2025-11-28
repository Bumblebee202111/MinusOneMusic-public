package com.github.bumblebee202111.minusonecloudmusic.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.PlaylistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM playlists WHERE id = :id")
    fun observePlaylist(id:Long): Flow<PlaylistEntity>

    @Query("SELECT * FROM playlists WHERE id IN (:ids)")
    fun observePlaylists(ids:List<Long>): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlists: List<PlaylistEntity>)

    @Query("DELETE FROM playlists WHERE id IN (:ids)")
    fun deletePlaylists(ids:List<Long>)
}