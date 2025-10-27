package com.github.bumblebee202111.minusonecloudmusic.ui.common

import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.ui.playlist.SongItemUiModel

abstract class BaseSongAdapter<VH : RecyclerView.ViewHolder>

    :
    ListAdapter<SongItemUiModel, VH>(
        SongItemUiModel.DIFF_CALLBACK
    ) {
    abstract val onItemClick: (position: Int) -> Unit
}