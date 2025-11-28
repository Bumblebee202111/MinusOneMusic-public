package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemNormalSongSimpleBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.playlist.SongItemUiModel


class SimpleSongAdapter(override val onItemClick: (position: Int) -> Unit) :
    BaseSongAdapter<SimpleSongAdapter.ViewHolder>(
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemNormalSongSimpleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val myRecentMusicData = getItem(position)
        holder.bind(myRecentMusicData) { onItemClick(position) }
    }

    class ViewHolder(private val binding: ListItemNormalSongSimpleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(song: SongItemUiModel, itemOnClickListener: View.OnClickListener) {
            binding.song = song
            binding.root.setOnClickListener(itemOnClickListener)
            binding.playingMark.apply {
                if (song.isBeingPlayed)
                    playAnimation()
                else
                    pauseAnimation()
            }
            binding.executePendingBindings()
        }
    }
}

