package com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "player_playlist_songs", indices = [Index(value = ["position"],unique = true)] )

data class PlayerPlaylistSongEntity(

    val position:Int,
    @PrimaryKey
    val id:Long,
)
