package com.github.bumblebee202111.minusonecloudmusic.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.PlayerPlaylistSongEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.view.GenericSongView

@Dao
interface PlayerDao {
    @Insert(entity = PlayerPlaylistSongEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSongs(playlistSongs: List<PlayerPlaylistSongEntity>)

    @Query("DELETE FROM player_playlist_songs")
    suspend fun deleteAllPlaylistSongs()

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """SELECT * FROM player_playlist_songs
LEFT OUTER JOIN
generic_songs
ON player_playlist_songs.id = generic_songs.id AND player_playlist_songs.is_local = generic_songs.is_local ORDER BY position"""
    )
    fun populatedPlaylistSongs(): PagingSource<Int, GenericSongView>

    @Query("SELECT COUNT(1) FROM player_playlist_songs")
    suspend fun getPlaylistSize():Int

    @Query("SELECT position FROM player_playlist_songs WHERE media_id=:mediaId")
    suspend fun getPlaylistSongPosition(mediaId: String): Int?
}