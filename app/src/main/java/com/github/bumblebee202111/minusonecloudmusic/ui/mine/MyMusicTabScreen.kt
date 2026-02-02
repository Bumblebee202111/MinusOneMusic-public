package com.github.bumblebee202111.minusonecloudmusic.ui.mine

import android.view.LayoutInflater
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.ListenRankRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.PlaylistRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
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

    DolphinTheme {
        Column {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { context ->
                    val tabLayout = LayoutInflater.from(context)
                        .inflate(R.layout.layout_my_music_tab_tab_layout, null) as TabLayout

                    val tabTexts = listOf(
                        R.string.my_music_title_created,
                        R.string.title_my_music_tab_collected,
                        R.string.title_my_music_tab_albums
                    )

                    val tabBadgeTextColor = context.getColor(R.color.colorText4)
                    val selectedTabBadgeTextColor = context.getColor(R.color.colorText1)

                    tabTexts.forEachIndexed { index, resId ->
                        val tab = tabLayout.newTab().setText(resId)
                        tab.orCreateBadge.apply {
                            backgroundColor = context.getColor(android.R.color.transparent)
                            badgeTextColor = if (index == tabLayout.selectedTabPosition)
                                selectedTabBadgeTextColor else tabBadgeTextColor
                        }
                        tabLayout.addTab(tab)
                    }

                    tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
                        override fun onTabSelected(tab: TabLayout.Tab?) {
                            tab?.let {
                                it.badge?.badgeTextColor = selectedTabBadgeTextColor
                                if (pagerState.currentPage != it.position) {
                                    scope.launch { pagerState.animateScrollToPage(it.position) }
                                }
                            }
                        }
                        override fun onTabUnselected(tab: TabLayout.Tab?) {
                            tab?.badge?.badgeTextColor = tabBadgeTextColor
                        }
                        override fun onTabReselected(tab: TabLayout.Tab?) {}
                    })

                    tabLayout
                },
                update = { tabLayout ->
                    myPlaylistTabs?.let { data ->
                        tabs.forEachIndexed { index, userPlaylistTab ->
                            tabLayout.getTabAt(index)?.badge?.number =
                                data[userPlaylistTab]?.size ?: 0
                        }
                    }
                    if (tabLayout.selectedTabPosition != pagerState.currentPage) {
                        tabLayout.getTabAt(pagerState.currentPage)?.select()
                    }
                }
            )

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