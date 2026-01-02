package com.github.bumblebee202111.minusonecloudmusic.ui.toplists

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.model.Billboard
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemBillboardBinding

class BillboardAdapter(private val navigateToPlaylist: (playlistId: Long) -> Unit) :
    ListAdapter<Billboard, BillboardAdapter.ViewHolder>(BillboardDiffUtil) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemBillboardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), navigateToPlaylist
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ListItemBillboardBinding,
        private val navigateToPlaylist: (playlistId: Long) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(billboard: Billboard) {
            binding.billboard = billboard
            binding.root.setOnClickListener {
                when (billboard.isMusicPlaylist) {
                    true -> navigateToPlaylist(billboard.id)
                    else -> Unit
                }
            }
            binding.executePendingBindings()
        }
    }
}

object BillboardDiffUtil : DiffUtil.ItemCallback<Billboard>() {
    override fun areItemsTheSame(
        oldItem: Billboard,
        newItem: Billboard
    ): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(
        oldItem: Billboard,
        newItem: Billboard
    ): Boolean {
        return oldItem == newItem
    }
}