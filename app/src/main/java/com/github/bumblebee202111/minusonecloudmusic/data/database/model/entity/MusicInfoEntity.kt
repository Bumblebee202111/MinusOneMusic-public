package com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.bumblebee202111.minusonecloudmusic.data.model.Album
import com.github.bumblebee202111.minusonecloudmusic.data.model.RemoteSong

@Entity
data class MusicInfoEntity(
    val name: String?,
    @PrimaryKey
    val id: Long,
    val album: Album?,
    val artists: List<String?>,
    val available: Boolean,
    val version: Int
)

fun MusicInfoEntity.asExternalModel(): RemoteSong = RemoteSong(
    name = name,
    id = id,
    album = album,
    artists = artists,
    available = available,
    version = version
)