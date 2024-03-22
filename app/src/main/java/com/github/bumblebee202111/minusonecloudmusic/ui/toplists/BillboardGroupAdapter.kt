package com.github.bumblebee202111.minusonecloudmusic.ui.toplists

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.data.model.MainPageBillboardRowGroup
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemBillboardGroupBinding

class BillboardGroupAdapter:ListAdapter<MainPageBillboardRowGroup, BillboardGroupAdapter.ViewHolder>(BillBoardGroupDiffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder( ListItemBillboardGroupBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    class ViewHolder(private val binding: ListItemBillboardGroupBinding) :RecyclerView.ViewHolder(binding.root){
        fun bind(billboardGroup: MainPageBillboardRowGroup) {
            binding.billboardGroup=billboardGroup
            binding.executePendingBindings()
        }

    }

}
object BillBoardGroupDiffUtil: DiffUtil.ItemCallback<MainPageBillboardRowGroup>() {
    override fun areItemsTheSame(oldItem: MainPageBillboardRowGroup, newItem: MainPageBillboardRowGroup): Boolean {
        return oldItem.billboards==newItem.billboards
    }

    override fun areContentsTheSame(oldItem: MainPageBillboardRowGroup, newItem: MainPageBillboardRowGroup): Boolean {
        return oldItem==newItem
    }

}