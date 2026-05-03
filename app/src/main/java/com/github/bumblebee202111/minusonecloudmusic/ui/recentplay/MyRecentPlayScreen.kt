package com.github.bumblebee202111.minusonecloudmusic.ui.recentplay

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bumblebee202111.minusonecloudmusic.ui.common.Toolbar
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRecentPlayScreen(
    onNavigateBack: () -> Unit,
    viewModel: MyRecentPlayViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()
    val tabs = listOf("歌曲", "歌单", "专辑", "视频")

    val recentPlaySongs by viewModel.recentPlaySongUiList.collectAsStateWithLifecycle(initialValue = emptyList())

    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        Toolbar(
            title = "Recent Play",
            onBackClick = onNavigateBack
        )

        SecondaryTabRow(
            selectedTabIndex = pagerState.currentPage
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                    text = {
                        if (index == 0 && (recentPlaySongs?.size ?: 0) > 0) {
                            BadgedBox(
                                badge = { Badge { Text(recentPlaySongs!!.size.toString()) } }
                            ) {
                                Text(title)
                            }
                        } else {
                            Text(title)
                        }
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> MyRecentPlayMusicTabContent(viewModel = viewModel)
                else -> MyRecentPlayMusicTabContent(viewModel = viewModel)
            }
        }
    }
}