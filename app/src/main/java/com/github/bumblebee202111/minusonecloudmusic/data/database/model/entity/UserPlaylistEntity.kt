package com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "user_playlists",
    primaryKeys = ["user_id", "playlist_no"]
)

data class UserPlaylistEntity(
    @ColumnInfo("user_id")
    val userId: Long,
    @ColumnInfo("playlist_id")
    val playlistId: Long,
    @ColumnInfo("playlist_no")
    val playlistNo:Int
)