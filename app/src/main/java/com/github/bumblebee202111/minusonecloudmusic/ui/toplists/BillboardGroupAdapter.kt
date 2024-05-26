package com.github.bumblebee202111.minusonecloudmusic.ui.toplists

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.data.model.MainPageBillboardRowGroup
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemBillboardGroupBinding
import com.google.android.material.divider.MaterialDividerItemDecoration

class BillboardGroupAdapter(private val navigateToPlaylist: (playlistId: Long) -> Unit) :
    ListAdapter<MainPageBillboardRowGroup, BillboardGroupAdapter.ViewHolder>(BillBoardGroupDiffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemBillboardGroupBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ).apply {
                billboards.apply {
                    addItemDecoration(
                        MaterialDividerItemDecoration(
                            context,
                            DividerItemDecoration.HORIZONTAL
                        ).apply {
                            dividerColor = Color.TRANSPARENT
                            setDividerThicknessResource(
                                context,
                                R.dimen.discover_block_list_item_spacing
                            )
                            isLastItemDecorated = false
                        })
                    adapter = BillboardAdapter(navigateToPlaylist)
                }
            })
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ListItemBillboardGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(billboardGroup: MainPageBillboardRowGroup) {
            binding.apply {
                setBillboardGroup(billboardGroup)
                billboards.apply {
                    val billboards = billboardGroup.billboards
                    if (billboards.isNotEmpty()) {
                        isVisible = true
                        (adapter as BillboardAdapter).submitList(billboards)
                    } else {
                        isGone = true
                    }
                }
                executePendingBindings()
            }
        }

    }

}

object BillBoardGroupDiffUtil : DiffUtil.ItemCallback<MainPageBillboardRowGroup>() {
    override fun areItemsTheSame(
        oldItem: MainPageBillboardRowGroup,
        newItem: MainPageBillboardRowGroup
    ): Boolean {
        return oldItem.billboards == newItem.billboards
    }

    override fun areContentsTheSame(
        oldItem: MainPageBillboardRowGroup,
        newItem: MainPageBillboardRowGroup
    ): Boolean {
        return oldItem == newItem
    }

}