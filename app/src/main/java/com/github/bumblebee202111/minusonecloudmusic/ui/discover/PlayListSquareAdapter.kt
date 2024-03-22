package com.github.bumblebee202111.minusonecloudmusic.ui.discover

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemDashboardItemBinding


class PlayListSquareAdapter(categories:List<String>,private val onItemClick:(position:Int)->Unit):ListAdapter<String, PlayListSquareAdapter.ViewHolder>(WowDashBoardItemDiffCallBack()) {

    init {
        submitList(categories)
    }

    class ViewHolder(private val binding:ListItemDashboardItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(category: String, itemOnClickListener: View.OnClickListener){
            binding.category=category
            binding.root.setOnClickListener(itemOnClickListener)
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ListItemDashboardItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)) {onItemClick(position)}
    }
}
class WowDashBoardItemDiffCallBack: DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem==newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem==newItem
    }
}