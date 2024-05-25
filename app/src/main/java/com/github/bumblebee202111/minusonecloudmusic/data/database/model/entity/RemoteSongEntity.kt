package com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.bumblebee202111.minusonecloudmusic.data.model.RemoteAlbum
import com.github.bumblebee202111.minusonecloudmusic.data.model.RemoteSong

@Entity
data class RemoteSongEntity(
    val name: String?,
    @PrimaryKey
    val id: Long,
    val album: RemoteAlbum?,
    val artists: List<String?>,
    val available: Boolean,
    @ColumnInfo("is_downloadable")
    val isDownloadable: Boolean,
) : AbstractSongEntity

fun RemoteSongEntity.asExternalModel() =
    RemoteSong(
        name = name,
        id = id,
        album = album,
        artists = artists,
        available = available,
        isDownloadable = isDownloadable,
    )

