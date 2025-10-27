package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemNormalSongWithPositionBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.playlist.SongItemUiModel

class SongWithPositionAdapter(override val onItemClick: ((position: Int) -> Unit)) :
    BaseSongAdapter<SongWithPositionAdapter.ViewHolder>() {
    class ViewHolder(private val binding: ListItemNormalSongWithPositionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            song: SongItemUiModel,
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
            ListItemNormalSongWithPositionBinding.inflate(
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
