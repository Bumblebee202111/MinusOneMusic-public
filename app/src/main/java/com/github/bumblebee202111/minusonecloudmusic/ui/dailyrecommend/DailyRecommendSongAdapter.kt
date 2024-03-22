package com.github.bumblebee202111.minusonecloudmusic.ui.dailyrecommend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.data.model.DailyRecommendSong
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemDailyRecommendBinding

class DailyRecommendSongAdapter(private val onItemClick: (DailyRecommendSong) -> Unit) : ListAdapter<DailyRecommendSong, DailyRecommendSongAdapter.ViewHolder>(DailyRecommendSongDiffUtil) {
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
        val playlist=getItem(position)
        holder.bind(playlist) { onItemClick(playlist) }
    }

    class ViewHolder(private val binding: ListItemDailyRecommendBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(dailyRecommendSong: DailyRecommendSong, itemOnClickListener: View.OnClickListener) {
            binding.song = dailyRecommendSong
            binding.root.setOnClickListener(itemOnClickListener)
            binding.executePendingBindings()
        }
    }
}

object DailyRecommendSongDiffUtil : DiffUtil.ItemCallback<DailyRecommendSong>() {
    override fun areItemsTheSame(oldItem: DailyRecommendSong, newItem: DailyRecommendSong): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DailyRecommendSong, newItem: DailyRecommendSong): Boolean {
        return oldItem == newItem
    }
}