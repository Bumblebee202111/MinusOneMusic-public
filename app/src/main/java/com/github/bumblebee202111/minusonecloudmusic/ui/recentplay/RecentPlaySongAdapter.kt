package com.github.bumblebee202111.minusonecloudmusic.ui.recentplay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemRecentPlaySongBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.playlist.PlaylistSongItemUiModel

class RecentPlaySongAdapter(private val onItemClick: (songId: Long) -> Unit) :
    ListAdapter<PlaylistSongItemUiModel, RecentPlaySongAdapter.ViewHolder>(
        PlaylistSongItemUiModel.DIFF_CALLBACK
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemRecentPlaySongBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val myRecentMusicData = getItem(position)
        holder.bind(myRecentMusicData) { onItemClick(myRecentMusicData.id) }
    }

    class ViewHolder(private val binding: ListItemRecentPlaySongBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(song: PlaylistSongItemUiModel, itemOnClickListener: View.OnClickListener) {
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

