package com.github.bumblebee202111.minusonecloudmusic.ui.playlist

import androidx.recyclerview.widget.DiffUtil
import com.github.bumblebee202111.minusonecloudmusic.data.model.AbstractRemoteSong
import com.github.bumblebee202111.minusonecloudmusic.data.model.Album

data class PlaylistSongItemUiModel(

    val name: String?,
    val id: Long,
    val album: Album?,
    val artists: List<String?>,
    val isCurrentSong:Boolean=false,
    val isBeingPlayed: Boolean = false
) {
    constructor(song: AbstractRemoteSong, isCurrentSong:Boolean=false, isBeingPlayed: Boolean = false) : this(

        name = song.name,
        id = song.id,
        album = song.album,
        artists = song.artists,
        isCurrentSong=isCurrentSong,
        isBeingPlayed = isBeingPlayed
    )

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PlaylistSongItemUiModel>() {
            override fun areItemsTheSame(
                oldItem: PlaylistSongItemUiModel,
                newItem: PlaylistSongItemUiModel
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: PlaylistSongItemUiModel,
                newItem: PlaylistSongItemUiModel
            ): Boolean {
                return oldItem.id == newItem.id && oldItem.isCurrentSong==newItem.isCurrentSong&&oldItem.isBeingPlayed == newItem.isBeingPlayed
            }
        }
    }
}