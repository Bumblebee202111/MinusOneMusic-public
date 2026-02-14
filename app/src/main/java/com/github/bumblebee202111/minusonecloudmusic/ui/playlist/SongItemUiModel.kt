package com.github.bumblebee202111.minusonecloudmusic.ui.playlist

import com.github.bumblebee202111.minusonecloudmusic.model.AbstractAlbum
import com.github.bumblebee202111.minusonecloudmusic.model.AbstractSong
data class SongItemUiModel(

    val name: String?,
    val mediaId: String,
    val album: AbstractAlbum?,
    val artists: List<String?>,
    val isCurrentSong:Boolean=false,
    val isBeingPlayed: Boolean = false,
) {
    constructor(
        song: AbstractSong,
        isCurrentSong: Boolean = false,
        isBeingPlayed: Boolean = false
    ) : this(
        name = song.name,
        mediaId = song.mediaId,
        album = song.album,
        artists = song.artists,
        isCurrentSong=isCurrentSong,
        isBeingPlayed = isBeingPlayed,
    )
}