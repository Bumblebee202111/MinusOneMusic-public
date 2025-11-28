package com.github.bumblebee202111.minusonecloudmusic.ui.playerhistory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemPlayerPlaylistSongBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.playlist.SongItemUiModel

class PagedPlayerSongAdapter(private val onItemClick: ((SongItemUiModel, Int) -> Unit)) :
    PagingDataAdapter<SongItemUiModel, PagedPlayerSongAdapter.ViewHolder>(SongItemUiModel.DIFF_CALLBACK) {
    class ViewHolder(private val binding: ListItemPlayerPlaylistSongBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            song: SongItemUiModel?,
            position: Int,
            itemOnClickListener: View.OnClickListener?
        ) {
            binding.song = song
            binding.position = position + 1
            binding.root.setOnClickListener(itemOnClickListener)
            with(binding.playingMark){
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
            ListItemPlayerPlaylistSongBinding.inflate(
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
                onItemClick(song,position)
            }
        }

    }

}
