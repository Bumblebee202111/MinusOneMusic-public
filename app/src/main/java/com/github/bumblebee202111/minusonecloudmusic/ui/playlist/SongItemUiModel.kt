package com.github.bumblebee202111.minusonecloudmusic.ui.playlist

import androidx.recyclerview.widget.DiffUtil
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

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SongItemUiModel>() {
            override fun areItemsTheSame(
                oldItem: SongItemUiModel,
                newItem: SongItemUiModel
            ): Boolean {
                return oldItem.mediaId == newItem.mediaId
            }

            override fun areContentsTheSame(
                oldItem: SongItemUiModel,
                newItem: SongItemUiModel
            ): Boolean {
                return oldItem.mediaId == newItem.mediaId && oldItem.isCurrentSong == newItem.isCurrentSong && oldItem.isBeingPlayed == newItem.isBeingPlayed
            }
        }
    }
}