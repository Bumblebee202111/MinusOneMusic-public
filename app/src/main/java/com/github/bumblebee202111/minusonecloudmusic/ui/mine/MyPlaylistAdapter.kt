package com.github.bumblebee202111.minusonecloudmusic.ui.mine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemUserPlaylistChartsBinding
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemUserPlaylistNormalBinding

class MyPlaylistAdapter(private val onItemClick: (UserPlaylistItem) -> Unit) :
    ListAdapter<UserPlaylistItem, RecyclerView.ViewHolder>(UserPlaylistItemDiffUtil) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_NORMAL_PLAYLIST -> UserNormalPlaylistViewHolder(
                ListItemUserPlaylistNormalBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            ITEM_VIEW_TYPE_CHARTS -> UserChartsViewHolder(
                ListItemUserPlaylistChartsBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> throw IllegalArgumentException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UserNormalPlaylistViewHolder -> {
                val normalPlaylistItem = getItem(position) as NormalPlaylistItem
                holder.bind(normalPlaylistItem) { onItemClick(normalPlaylistItem) }
            }

            is UserChartsViewHolder -> {
                val userChartsItem = getItem(position) as UserChartsItem
                holder.bind(userChartsItem) { onItemClick(userChartsItem) }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is NormalPlaylistItem -> ITEM_VIEW_TYPE_NORMAL_PLAYLIST
            is UserChartsItem -> ITEM_VIEW_TYPE_CHARTS
        }
    }

    class UserNormalPlaylistViewHolder(private val binding: ListItemUserPlaylistNormalBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            normalPlaylistItem: NormalPlaylistItem,
            itemOnClickListener: View.OnClickListener
        ) {
            binding.playlist = normalPlaylistItem.playlist
            binding.root.setOnClickListener(itemOnClickListener)
            binding.executePendingBindings()
        }
    }

    class UserChartsViewHolder(private val binding: ListItemUserPlaylistChartsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(userChartsItem: UserChartsItem, itemOnClickListener: View.OnClickListener) {
            binding.listenSongs = userChartsItem.listenSongs
            binding.root.setOnClickListener(itemOnClickListener)
            binding.executePendingBindings()
        }
    }

    companion object {
        const val ITEM_VIEW_TYPE_NORMAL_PLAYLIST = 0
        const val ITEM_VIEW_TYPE_CHARTS = 1
    }
}

object UserPlaylistItemDiffUtil : DiffUtil.ItemCallback<UserPlaylistItem>() {
    override fun areItemsTheSame(
        oldItem: UserPlaylistItem,
        newItem: UserPlaylistItem
    ): Boolean {
        return if (oldItem is UserChartsItem && newItem is UserChartsItem) {
            true
        } else {
            oldItem is NormalPlaylistItem && newItem is NormalPlaylistItem && oldItem.playlist.id == newItem.playlist.id
        }
    }

    override fun areContentsTheSame(
        oldItem: UserPlaylistItem,
        newItem: UserPlaylistItem
    ): Boolean {
        return if ((oldItem is UserChartsItem && newItem is UserChartsItem) || (oldItem is NormalPlaylistItem && newItem is NormalPlaylistItem)) {
            oldItem == newItem
        } else {
            false
        }
    }
}