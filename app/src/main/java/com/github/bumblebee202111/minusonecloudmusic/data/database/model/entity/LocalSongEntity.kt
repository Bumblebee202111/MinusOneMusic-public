package com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.bumblebee202111.minusonecloudmusic.data.model.LocalAlbum
import com.github.bumblebee202111.minusonecloudmusic.data.model.LocalSong

@Entity
data class LocalSongEntity(
    val name: String?,
    @PrimaryKey
    val id: Long,
    val album: LocalAlbum?,
    val artists: List<String?>,
    val available: Boolean,
)

fun LocalSongEntity.asExternalModel() = LocalSong(
    name = name,
    id = id,
    album = album,
    artists = artists,
    available = available
)