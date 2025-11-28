package com.github.bumblebee202111.minusonecloudmusic.ui.discover

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.model.DiscoverBlock
import com.github.bumblebee202111.minusonecloudmusic.databinding.ItemDiscoverDragonBallBinding
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemDiscoverBlockPlaylistsBinding
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemDiscoverBlockTopListsBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.playlist.PlaylistFragment
import com.google.android.material.divider.MaterialDividerItemDecoration

class DiscoverBlockAdapter(
    private val onDragonBallClick: (DiscoverBlock.DragonBalls.DragonBall.Type) -> Unit,
    private val onPlaylistClick: (playlistId: Long, playlistCreatorId: Long, isMyPL: Boolean) -> Unit
) :
    ListAdapter<DiscoverBlock, DiscoverBlockAdapter.ViewHolder>(DiscoverBlockDiffCallback) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DiscoverBlock.DragonBalls -> R.layout.list_item_discover_block_dragon_balls
            is DiscoverBlock.Playlists -> R.layout.list_item_discover_block_playlists
            is DiscoverBlock.TopLists -> R.layout.list_item_discover_block_top_lists
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.list_item_discover_block_dragon_balls ->
                ViewHolder.DragonBallsHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.list_item_discover_block_dragon_balls, parent, false),
                    onDragonBallClick
                )

            R.layout.list_item_discover_block_playlists -> {
                val binding = ListItemDiscoverBlockPlaylistsBinding.inflate(
                    inflater,
                    parent,
                    false
                ).apply {
                    playlistList.apply {
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
                    }
                }
                ViewHolder.PlaylistsHolder(binding) { playlistId, playlistCreatorId ->
                    onPlaylistClick(
                        playlistId,
                        playlistCreatorId,
                        true
                    )
                }
            }

            R.layout.list_item_discover_block_top_lists -> {
                val binding = ListItemDiscoverBlockTopListsBinding.inflate(
                    inflater,
                    parent,
                    false
                ).apply {
                    topListList.apply {
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
                    }
                }
                ViewHolder.TopListsHolder(binding) {
                    onPlaylistClick(
                        it,
                        PlaylistFragment.ARG_VALUE_PLAYLIST_CREATOR_ID_UNKNOWN,
                        false
                    )
                }
            }


            else -> throw IllegalArgumentException("Invalid viewType")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.DragonBallsHolder -> {
                holder.bind(getItem(position) as DiscoverBlock.DragonBalls)
            }

            is ViewHolder.PlaylistsHolder -> {
                holder.bind(getItem(position) as DiscoverBlock.Playlists)
            }

            is ViewHolder.TopListsHolder -> {
                holder.bind(getItem(position) as DiscoverBlock.TopLists)
            }
        }
    }

    sealed class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        class DragonBallsHolder(
            itemView: View,
            private val onDragonBallClick: (DiscoverBlock.DragonBalls.DragonBall.Type) -> Unit
        ) : ViewHolder(itemView) {
            fun bind(dragonBallBlock: DiscoverBlock.DragonBalls) {
                val dragonBalls = dragonBallBlock.dragonBalls
                (itemView as LinearLayout).apply {
                    removeAllViews()
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    weightSum = dragonBalls.size.toFloat()
                    dragonBalls.forEach { db ->
                        val binding = ItemDiscoverDragonBallBinding.inflate(
                            LayoutInflater.from(context),
                            this,
                            false
                        ).apply {
                            dragonBall = db
                        }
                        val root = binding.root.apply {
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1.0f
                            )
                            setOnClickListener {
                                onDragonBallClick(db.type)
                            }
                        }
                        post { addView(root) }
                    }
                }
            }
        }

        class PlaylistsHolder(
            private val binding: ListItemDiscoverBlockPlaylistsBinding,
            private val onPlaylistClick: (playlistId: Long, playlistCreatorId: Long) -> Unit
        ) : ViewHolder(binding.root) {
            fun bind(playlists: DiscoverBlock.Playlists) {
                binding.apply {
                    playlistsBlock = playlists
                    playlistList.apply {
                        playlists.playlists.let { playlists ->
                            if (playlists.isNotEmpty()) {
                                isVisible = true
                                adapter = (adapter as? DiscoverBlockPlaylistAdapter
                                    ?: DiscoverBlockPlaylistAdapter(onPlaylistClick))
                                    .apply {
                                        submitList(playlists)
                                    }
                            } else {
                                isGone = true
                            }
                        }
                    }
                    executePendingBindings()
                }
            }
        }

        class TopListsHolder(
            private val binding: ListItemDiscoverBlockTopListsBinding,
            private val onTopListClick: (playlistId: Long) -> Unit
        ) : ViewHolder(binding.root) {
            fun bind(topLists: DiscoverBlock.TopLists) {
                binding.apply {
                    topListsBlock = topLists
                    topListList.apply {
                        topLists.topLists.let { topListList ->
                            if (topListList.isNotEmpty()) {
                                isVisible = true
                                adapter = (adapter as? DiscoverBlockTopListAdapter
                                    ?: DiscoverBlockTopListAdapter(onTopListClick))
                                    .apply {
                                        submitList(topListList)
                                    }
                            } else {
                                isGone = true
                            }
                        }
                    }

                    executePendingBindings()
                }
            }
        }
    }

}

object DiscoverBlockDiffCallback : DiffUtil.ItemCallback<DiscoverBlock>() {
    override fun areItemsTheSame(
        oldItem: DiscoverBlock,
        newItem: DiscoverBlock
    ): Boolean {
        return when {
            oldItem is DiscoverBlock.DragonBalls && newItem is DiscoverBlock.DragonBalls || oldItem is DiscoverBlock.Playlists && newItem is DiscoverBlock.Playlists || oldItem is DiscoverBlock.TopLists && newItem is DiscoverBlock.TopLists ->
                true

            else -> false
        }
    }

    override fun areContentsTheSame(
        oldItem: DiscoverBlock,
        newItem: DiscoverBlock
    ): Boolean {
        return when {
            oldItem is DiscoverBlock.Playlists && newItem is DiscoverBlock.Playlists -> oldItem == newItem
            else -> true
        }
    }
}