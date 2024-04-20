package com.github.bumblebee202111.minusonecloudmusic.ui.playlist

import android.view.View
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class BasePlaylistSongAdapter<VH : RecyclerView.ViewHolder>

    :
    ListAdapter<PlaylistSongItemUiModel, VH>(
        PlaylistSongItemUiModel.DIFF_CALLBACK
    ) {
    abstract val onItemClick: (position: Int) -> Unit
}