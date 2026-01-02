package com.github.bumblebee202111.minusonecloudmusic.ui.discover

import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.databinding.ItemDiscoverDragonBallBinding
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemDiscoverBlockPlaylistsBinding
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemDiscoverBlockTopListsBinding
import com.github.bumblebee202111.minusonecloudmusic.model.DiscoverBlock
import com.google.android.material.divider.MaterialDividerItemDecoration

@Composable
fun DragonBallBlockView(
    block: DiscoverBlock.DragonBalls,
    onDragonBallClick: (DiscoverBlock.DragonBalls.DragonBall.Type) -> Unit
) {
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            LayoutInflater.from(context)
                .inflate(R.layout.list_item_discover_block_dragon_balls, null, false) as LinearLayout
        },
        update = { view ->
            val dragonBalls = block.dragonBalls
            view.removeAllViews()
            view.weightSum = dragonBalls.size.toFloat()
            
            dragonBalls.forEach { db ->
                val binding = ItemDiscoverDragonBallBinding.inflate(
                    LayoutInflater.from(view.context),
                    view,
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
                view.addView(root)
            }
        }
    )
}

@Composable
fun PlaylistBlockView(
    block: DiscoverBlock.Playlists,
    onPlaylistClick: (playlistId: Long, playlistCreatorId: Long) -> Unit
) {
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            val binding = ListItemDiscoverBlockPlaylistsBinding.inflate(LayoutInflater.from(context))

            binding.playlistList.apply {
                addItemDecoration(
                    MaterialDividerItemDecoration(context, DividerItemDecoration.HORIZONTAL).apply {
                        dividerColor = android.graphics.Color.TRANSPARENT
                        setDividerThicknessResource(context, R.dimen.discover_block_list_item_spacing)
                        isLastItemDecorated = false
                    }
                )
                adapter = DiscoverBlockPlaylistAdapter(onPlaylistClick)
            }
            binding.root
        },
        update = { view ->
            val binding = DataBindingUtil.getBinding<ListItemDiscoverBlockPlaylistsBinding>(view)!!
            
            binding.playlistsBlock = block
            binding.playlistList.apply {
                val playlists = block.playlists
                if (playlists.isNotEmpty()) {
                    isVisible = true
                    (adapter as? DiscoverBlockPlaylistAdapter)?.submitList(playlists)
                } else {
                    isGone = true
                }
            }
            binding.executePendingBindings()
        }
    )
}

@Composable
fun TopListBlockView(
    block: DiscoverBlock.TopLists,
    onTopListClick: (playlistId: Long) -> Unit
) {
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            val binding = ListItemDiscoverBlockTopListsBinding.inflate(LayoutInflater.from(context))

            binding.topListList.apply {
                addItemDecoration(
                    MaterialDividerItemDecoration(context, DividerItemDecoration.HORIZONTAL).apply {
                        dividerColor = android.graphics.Color.TRANSPARENT
                        setDividerThicknessResource(context, R.dimen.discover_block_list_item_spacing)
                        isLastItemDecorated = false
                    }
                )
                adapter = DiscoverBlockTopListAdapter(onTopListClick)
            }
            binding.root
        },
        update = { view ->
            val binding = DataBindingUtil.getBinding<ListItemDiscoverBlockTopListsBinding>(view)!!

            binding.topListsBlock = block
            binding.topListList.apply {
                val topLists = block.topLists
                if (topLists.isNotEmpty()) {
                    isVisible = true
                    (adapter as? DiscoverBlockTopListAdapter)?.submitList(topLists)
                } else {
                    isGone = true
                }
            }
            binding.executePendingBindings()
        }
    )
}