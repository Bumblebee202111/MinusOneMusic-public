package com.github.bumblebee202111.minusonecloudmusic.ui.recentplay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.data.model.MyRecentMusicData
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemRecentPlaySongBinding

class RecentPlaySongAdapter(private val onItemClick: (MyRecentMusicData) -> Unit) : ListAdapter<MyRecentMusicData, RecentPlaySongAdapter.ViewHolder>(
    MyRecentMusicDataDiffUtil
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
        val myRecentMusicData=getItem(position)
        holder.bind(myRecentMusicData) { onItemClick(myRecentMusicData) }
    }

    class ViewHolder(private val binding: ListItemRecentPlaySongBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(myRecentMusicData: MyRecentMusicData, itemOnClickListener: View.OnClickListener) {
            binding.song = myRecentMusicData.musicInfo
            binding.root.setOnClickListener(itemOnClickListener)
            binding.executePendingBindings()
        }
    }
}


object MyRecentMusicDataDiffUtil : DiffUtil.ItemCallback<MyRecentMusicData>() {
    override fun areItemsTheSame(oldItem: MyRecentMusicData, newItem: MyRecentMusicData): Boolean {
        return oldItem.musicInfo.id == newItem.musicInfo.id
    }

    override fun areContentsTheSame(oldItem: MyRecentMusicData, newItem: MyRecentMusicData): Boolean {
        return oldItem == newItem
    }
}