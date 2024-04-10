package com.github.bumblebee202111.minusonecloudmusic.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.MusicInfoEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.PlayerPlaylistSongEntity

@Dao
interface PlayerDao {
    @Insert(entity = PlayerPlaylistSongEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSongs(playlistSongs: List<PlayerPlaylistSongEntity>)

    @Query("DELETE FROM player_playlist_songs")
    suspend fun deleteAllPlaylistSongs()

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM player_playlist_songs LEFT OUTER JOIN MusicInfoEntity WHERE player_playlist_songs.id = MusicInfoEntity.id ORDER BY position")
    fun populatedPlaylistSongs(): PagingSource<Int, MusicInfoEntity>

    @Query("SELECT COUNT(1) FROM player_playlist_songs")
    suspend fun getPlaylistSize():Int

    @Query("SELECT position FROM player_playlist_songs WHERE id=:songId")
    suspend fun getPlaylistSongPosition(songId: Long): Int
}