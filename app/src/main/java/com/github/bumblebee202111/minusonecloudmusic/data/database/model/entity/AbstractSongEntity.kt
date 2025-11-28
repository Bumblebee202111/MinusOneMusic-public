package com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity

import com.github.bumblebee202111.minusonecloudmusic.model.AbstractSong

sealed interface AbstractSongEntity

fun AbstractSongEntity.toAbstractSong(): AbstractSong = when (this) {
    is RemoteSongEntity -> toRemoteSong()
    is LocalSongEntity -> toLocalSong()
}