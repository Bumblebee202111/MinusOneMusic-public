package com.github.bumblebee202111.minusonecloudmusic.ui.playlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemPlaylistSongWithAlbumBinding

class PlaylistSongWithAlbumAdapter(override val onItemClick: (position: Int) -> Unit) :
    BasePlaylistSongAdapter<PlaylistSongWithAlbumAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemPlaylistSongWithAlbumBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = getItem(position)
        holder.bind(song) { onItemClick(position) }
    }

    class ViewHolder(private val binding: ListItemPlaylistSongWithAlbumBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            song: PlaylistSongItemUiModel,
            itemOnClickListener: View.OnClickListener
        ) {
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
