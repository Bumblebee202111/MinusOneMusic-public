package com.github.bumblebee202111.minusonecloudmusic.ui.playlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemPlaylistSongBinding

class PagedSongAdapter(private val onItemClick: ((PlaylistSongItemUiModel) -> Unit)) :
    PagingDataAdapter<PlaylistSongItemUiModel, PagedSongAdapter.ViewHolder>(PlaylistSongItemUiModel.DIFF_CALLBACK) {
    class ViewHolder(private val binding: ListItemPlaylistSongBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(song: PlaylistSongItemUiModel?, position: Int, itemOnClickListener: View.OnClickListener?) {
            binding.song = song
            binding.position = position + 1
            binding.root.setOnClickListener(itemOnClickListener)
            binding.playingMark.apply{
                if(song?.isBeingPlayed == true)
                    playAnimation()
                else
                    pauseAnimation()
            }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemPlaylistSongBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = getItem(position)
        holder.bind(song, position) {
            if (song != null) {
                onItemClick(song)
            }
        }

    }

}
