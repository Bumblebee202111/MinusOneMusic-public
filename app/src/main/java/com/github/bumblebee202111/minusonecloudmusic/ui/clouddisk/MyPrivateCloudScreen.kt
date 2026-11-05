package com.github.bumblebee202111.minusonecloudmusic.ui.clouddisk

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.github.bumblebee202111.minusonecloudmusic.ui.LocalMiniPlayerBarHeight
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PagedSongWithPositionList
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PlaylistPlayAllActions
import com.github.bumblebee202111.minusonecloudmusic.ui.common.Toolbar
@Composable
fun MyPrivateCloudScreen(
    onNavigateBack: () -> Unit,
    viewModel: MyPrivateCloudViewModel = hiltViewModel()
) {
    val songs = viewModel.songUiItemsPagingData.collectAsLazyPagingItems()
    val cloudSongsCount by viewModel.cloudSongsCount.collectAsStateWithLifecycle(initialValue = 0)

    val bottomPadding = LocalMiniPlayerBarHeight.current

    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        Toolbar(
            title = "音乐云盘",
            onBackClick = onNavigateBack,
            modifier = Modifier.statusBarsPadding()
        )

        PlaylistPlayAllActions(
            count = cloudSongsCount,
            onClick = { viewModel.playAll() }
        )

        PagedSongWithPositionList(
            songs = songs,
            onItemClick = viewModel::onSongItemClick,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = bottomPadding)
        )
    }
}