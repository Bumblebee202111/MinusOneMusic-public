package com.github.bumblebee202111.minusonecloudmusic.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.PlayerPlaylistSongEntity

@Dao
interface PlayerDao {
    @Insert(entity = PlayerPlaylistSongEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSongs(playlistSongs: List<PlayerPlaylistSongEntity>)

    @Query("DELETE FROM player_playlist_songs")
    suspend fun deleteAllPlaylistSongs()

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        QUERY_PLAYER_PLAYLIST_SONGS
    )
    fun populatedPlaylistSongsPagingSource(): PagingSource<Int, PlayerPlaylistSongEntity>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        QUERY_PLAYER_PLAYLIST_SONGS
    )
    suspend fun populatedPlaylistSongs(): List<PlayerPlaylistSongEntity>

    @Query("SELECT COUNT(1) FROM player_playlist_songs")
    suspend fun getPlaylistSize():Int

    @Query("SELECT position FROM player_playlist_songs WHERE media_id=:mediaId")
    suspend fun getPlaylistSongPosition(mediaId: String): Int?

    @Query(
        """SELECT * FROM player_playlist_songs WHERE media_id = :mediaId"""
    )
    suspend fun getPlaylistSong(mediaId: String): PlayerPlaylistSongEntity

    private companion object {
        const val QUERY_PLAYER_PLAYLIST_SONGS =
            "SELECT * FROM player_playlist_songs ORDER BY position"
    }
}