package com.github.bumblebee202111.minusonecloudmusic.ui.mycollection

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.data.model.Video
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemMyCollectedMvBinding

class MyCollectedMvAdapter: ListAdapter<Video, MyCollectedMvAdapter.ViewHolder>(VideoDiffUtil) {
    class ViewHolder(private val binding:ListItemMyCollectedMvBinding):RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Video) {
            binding.video=item
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ListItemMyCollectedMvBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
object VideoDiffUtil:DiffUtil.ItemCallback<Video>(){
    override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean {
        return oldItem.id==newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean {
        return oldItem == newItem
    }

}