package com.github.bumblebee202111.minusonecloudmusic.ui.mycollection

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.data.model.RemoteAlbum
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemMyAlbumBinding

class MyAlbumAdapter : ListAdapter<RemoteAlbum, MyAlbumAdapter.ViewHolder>(AlbumDiffUtil) {
    class ViewHolder(private val binding:ListItemMyAlbumBinding):RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RemoteAlbum) {
            binding.album=item
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ListItemMyAlbumBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

object AlbumDiffUtil : DiffUtil.ItemCallback<RemoteAlbum>() {
    override fun areItemsTheSame(oldItem: RemoteAlbum, newItem: RemoteAlbum): Boolean {
        return oldItem.id==newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: RemoteAlbum, newItem: RemoteAlbum): Boolean {
        return oldItem == newItem
    }

}