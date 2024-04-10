package com.github.bumblebee202111.minusonecloudmusic.ui.dailyrecommend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemDailyRecommendBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.playlist.PlaylistSongItemUiModel

class DailyRecommendSongAdapter(private val onItemClick: (PlaylistSongItemUiModel) -> Unit) :
    ListAdapter<PlaylistSongItemUiModel, DailyRecommendSongAdapter.ViewHolder>(
        PlaylistSongItemUiModel.DIFF_CALLBACK
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemDailyRecommendBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = getItem(position)
        holder.bind(playlist) { onItemClick(playlist) }
    }

    class ViewHolder(private val binding: ListItemDailyRecommendBinding) :
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
