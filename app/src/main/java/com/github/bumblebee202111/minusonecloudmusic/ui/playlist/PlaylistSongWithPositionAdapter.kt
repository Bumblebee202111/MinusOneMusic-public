package com.github.bumblebee202111.minusonecloudmusic.ui.playlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemPlaylistSongWithPositionBinding

class PlaylistSongWithPositionAdapter(override val onItemClick: ((position: Int) -> Unit)) :
    BasePlaylistSongAdapter<PlaylistSongWithPositionAdapter.ViewHolder>() {
    class ViewHolder(private val binding: ListItemPlaylistSongWithPositionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            song: PlaylistSongItemUiModel,
            position: Int,
            itemOnClickListener: View.OnClickListener
        ) {
            binding.song = song
            binding.position = position + 1
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemPlaylistSongWithPositionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = getItem(position)
        holder.bind(song, position) {
            onItemClick(position)
        }
    }

}
