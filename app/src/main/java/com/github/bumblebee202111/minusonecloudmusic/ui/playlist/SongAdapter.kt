package com.github.bumblebee202111.minusonecloudmusic.ui.playlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.data.model.Song
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemSongBinding

class SongAdapter(private val onItemClick: ((Song) -> Unit)?) :
    ListAdapter<Song, SongAdapter.ViewHolder>(SongDiffCallback) {
    class ViewHolder(private val binding: ListItemSongBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Song, position: Int, itemOnClickListener: View.OnClickListener?) {
            binding.song = song
            binding.position = position + 1
            binding.root.setOnClickListener(itemOnClickListener)
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemSongBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = getItem(position)
        holder.bind(song, position) { onItemClick?.let { it -> it(song) } }

    }

}

object SongDiffCallback : DiffUtil.ItemCallback<Song>() {
    override fun areItemsTheSame(
        oldItem: Song,
        newItem: Song
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Song,
        newItem: Song
    ): Boolean {
        return oldItem == newItem
    }
}