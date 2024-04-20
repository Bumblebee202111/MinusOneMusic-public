package com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "player_playlist_songs", indices = [Index(value = ["position"],unique = true)] )
data class PlayerPlaylistSongEntity(
    @PrimaryKey
    val position: Int,

    @ColumnInfo("media_id")
    val mediaId: String,
    @ColumnInfo("is_local")
    val isLocal: Boolean,
    val id:Long,

    )
