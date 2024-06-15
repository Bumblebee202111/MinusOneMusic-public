package com.github.bumblebee202111.minusonecloudmusic.ui.common.songadapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemNormalSongWithAlbumBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.playlist.SongItemUiModel

class SongWithAlbumAdapter(override val onItemClick: (position: Int) -> Unit) :
    BaseSongAdapter<SongWithAlbumAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemNormalSongWithAlbumBinding.inflate(
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

    class ViewHolder(private val binding: ListItemNormalSongWithAlbumBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            song: SongItemUiModel,
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
