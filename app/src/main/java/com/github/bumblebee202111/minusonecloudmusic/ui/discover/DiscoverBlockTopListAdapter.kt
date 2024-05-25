package com.github.bumblebee202111.minusonecloudmusic.ui.discover

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.data.model.DiscoverBlock
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemDiscoverBlockTopListBinding

class DiscoverBlockTopListAdapter(private val onTopListClick: (playlistId: Long) -> Unit) :
    ListAdapter<DiscoverBlock.TopLists.TopList, DiscoverBlockTopListAdapter.ViewHolder>(
        DiscoverBlockTopListDiffCallback
    ) {

    class ViewHolder(
        private val binding: ListItemDiscoverBlockTopListBinding,
        private val onTopListClick: (playlistId: Long) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(topList: DiscoverBlock.TopLists.TopList) {
            binding.apply {
                setTopList(topList)
                root.setOnClickListener {
                    onTopListClick(topList.id)
                }
                executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemDiscoverBlockTopListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onTopListClick
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

object DiscoverBlockTopListDiffCallback : DiffUtil.ItemCallback<DiscoverBlock.TopLists.TopList>() {
    override fun areItemsTheSame(
        oldItem: DiscoverBlock.TopLists.TopList,
        newItem: DiscoverBlock.TopLists.TopList
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: DiscoverBlock.TopLists.TopList,
        newItem: DiscoverBlock.TopLists.TopList
    ): Boolean {
        return oldItem == newItem
    }
}