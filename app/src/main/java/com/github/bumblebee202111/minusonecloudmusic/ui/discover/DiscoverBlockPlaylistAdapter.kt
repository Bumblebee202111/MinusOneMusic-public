package com.github.bumblebee202111.minusonecloudmusic.ui.discover

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.data.model.DiscoverBlock
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemDiscoverBlockPlaylistBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.playlist.PlaylistFragment

class DiscoverBlockPlaylistAdapter(private val onPlaylistClick: (playlistId: Long, playlistCreatorId: Long) -> Unit) :
    ListAdapter<DiscoverBlock.Playlist, DiscoverBlockPlaylistAdapter.ViewHolder>(
        DiscoverBlockPlaylistDiffCallback
    ) {

    class ViewHolder(
        private val binding: ListItemDiscoverBlockPlaylistBinding,
        private val onPlaylistClick: (playlistId: Long, playlistCreatorId: Long) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(playlist: DiscoverBlock.Playlist) {
            binding.apply {
                setPlaylist(playlist)
                root.setOnClickListener {
                    onPlaylistClick(
                        playlist.id,
                        PlaylistFragment.ARG_VALUE_PLAYLIST_CREATOR_ID_UNKNOWN
                    )
                }
                executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemDiscoverBlockPlaylistBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onPlaylistClick
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

object DiscoverBlockPlaylistDiffCallback : DiffUtil.ItemCallback<DiscoverBlock.Playlist>() {
    override fun areItemsTheSame(
        oldItem: DiscoverBlock.Playlist,
        newItem: DiscoverBlock.Playlist
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: DiscoverBlock.Playlist,
        newItem: DiscoverBlock.Playlist
    ): Boolean {
        return oldItem == newItem
    }
}