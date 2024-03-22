package com.github.bumblebee202111.minusonecloudmusic.ui.localmusic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.data.model.LocalSong
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemLocalSongBinding

class LocalSongAdapter(private val onItemClick: (LocalSong) -> Unit) : ListAdapter<LocalSong, LocalSongAdapter.ViewHolder>(
    LocalSongDiffUtil
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemLocalSongBinding.inflate(
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

    class ViewHolder(private val binding: ListItemLocalSongBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(song: LocalSong, itemOnClickListener: View.OnClickListener) {
            binding.song = song
            binding.root.setOnClickListener(itemOnClickListener)
            binding.executePendingBindings()
        }
    }
}


object LocalSongDiffUtil : DiffUtil.ItemCallback<LocalSong>() {
    override fun areItemsTheSame(oldItem: LocalSong, newItem: LocalSong): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: LocalSong, newItem: LocalSong): Boolean {
        return oldItem == newItem
    }
}