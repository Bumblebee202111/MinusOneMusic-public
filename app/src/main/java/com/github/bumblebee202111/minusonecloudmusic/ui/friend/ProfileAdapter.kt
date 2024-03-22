package com.github.bumblebee202111.minusonecloudmusic.ui.friend

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.data.model.FollowedUserProfile
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemUserFollowBinding

class ProfileAdapter: ListAdapter<FollowedUserProfile, ProfileAdapter.ViewHolder>(ProfileDiffUtil) {
    class ViewHolder(private val binding:ListItemUserFollowBinding):RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FollowedUserProfile) {
            binding.followingUser=item
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ListItemUserFollowBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
object ProfileDiffUtil:DiffUtil.ItemCallback<FollowedUserProfile>(){
    override fun areItemsTheSame(oldItem: FollowedUserProfile, newItem: FollowedUserProfile): Boolean {
        return oldItem.userId==newItem.userId
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: FollowedUserProfile, newItem: FollowedUserProfile): Boolean {
        return oldItem == newItem
    }

}