package com.github.bumblebee202111.minusonecloudmusic.ui.mine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.ListenRankRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.PlaylistRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme
import kotlinx.coroutines.launch

@Composable
fun MyMusicTabScreen(
    viewModel: MineViewModel,
    onNavigate: (NavKey) -> Unit
) {
    val myPlaylistTabs by viewModel.myPlaylistTabs.collectAsStateWithLifecycle(initialValue = emptyMap())
    val tabs = UserPlaylistTab.entries
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    val tabTexts = listOf(
        R.string.my_music_title_created,
        R.string.title_my_music_tab_collected,
        R.string.title_my_music_tab_albums
    )

    DolphinTheme {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, userPlaylistTab ->
                    val selected = pagerState.currentPage == index
                    val count = myPlaylistTabs?.get(userPlaylistTab)?.size ?: 0
                    val textColor = if (selected) DolphinTheme.colors.text1 else DolphinTheme.colors.text4

                    Box(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { scope.launch { pagerState.animateScrollToPage(index) } }
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        BadgedBox(
                            badge = {
                                if (count > 0) {
                                    Text(
                                        text = count.toString(),
                                        color = textColor,
                                        fontSize = 11.sp,
                                        modifier = Modifier.offset(x = 10.dp, y = (-2).dp)
                                    )
                                }
                            }
                        ) {
                            Text(
                                text = stringResource(id = tabTexts[index]),
                                color = textColor,
                                fontSize = 15.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                UserPlaylistTabScreen(
                    viewModel = viewModel,
                    category = tabs[page],
                    onItemClick = { userPlaylistItem ->
                        val route = when (userPlaylistItem) {
                            is NormalPlaylistItem -> {
                                PlaylistRoute(
                                    playlistId = userPlaylistItem.playlist.id,
                                    playlistCreatorId = userPlaylistItem.playlist.creatorId ?: 0,
                                    isMyPL = true
                                )
                            }
                            is UserChartsItem -> {
                                ListenRankRoute(userPlaylistItem.userId)
                            }
                        }
                        onNavigate(route)
                    }
                )
            }
        }
    }
}