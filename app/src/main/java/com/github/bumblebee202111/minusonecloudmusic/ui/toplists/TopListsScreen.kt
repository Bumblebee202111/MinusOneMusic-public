package com.github.bumblebee202111.minusonecloudmusic.ui.toplists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemBillboardBinding
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemBillboardGroupBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.Toolbar
import com.github.bumblebee202111.minusonecloudmusic.ui.common.loadImage
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme

@Composable
fun TopListsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlaylist: (Long) -> Unit,
    viewModel: TopListsViewModel = hiltViewModel()
) {
    val topLists by viewModel.topLists.collectAsStateWithLifecycle(initialValue = emptyList())

    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        Toolbar(
            title = "排行榜",
            onBackClick = onNavigateBack
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(topLists ?: emptyList()) { billboardGroup ->
                AndroidViewBinding(ListItemBillboardGroupBinding::inflate) {
                    this.billboardCategory.text = billboardGroup.name
                    this.billboardCategory.paint.isFakeBoldText = true

                    val billboardsList = billboardGroup.billboards
                    if (billboardsList.isNotEmpty()) {
                        this.billboards.isVisible = true
                        this.billboards.apply {
                            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                            setContent {
                                DolphinTheme {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        items(billboardsList) { billboard ->
                                            AndroidViewBinding(ListItemBillboardBinding::inflate) {
                                                this.billboard = billboard
                                                this.billboardCover.loadImage(billboard.coverImgUrl, thumbnailSize = 137, quality = 80)

                                                this.root.setOnClickListener {
                                                    if (billboard.isMusicPlaylist) {
                                                        onNavigateToPlaylist(billboard.id)
                                                    }
                                                }
                                                executePendingBindings()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        this.billboards.isGone = true
                    }
                }
            }
        }
    }
}