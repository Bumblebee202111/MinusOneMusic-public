package com.github.bumblebee202111.minusonecloudmusic.ui.discover

import DiscoverList
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bumblebee202111.minusonecloudmusic.model.DiscoverBlock

@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onDragonBallClick: (DiscoverBlock.DragonBalls.DragonBall.Type) -> Unit,
    onPlaylistClick: (Long, Long, Boolean) -> Unit
) {
    val blocks by viewModel.blocks.collectAsStateWithLifecycle()

    Scaffold(

        containerColor = Color(0xFFF7F9FC),
        topBar = {
            DiscoverTopBar(
                onMenuClick = onMenuClick,
                onSearchClick = onSearchClick
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            DiscoverList(
                blocks = blocks,
                onDragonBallClick = onDragonBallClick,
                onPlaylistClick = onPlaylistClick
            )
        }
    }
}