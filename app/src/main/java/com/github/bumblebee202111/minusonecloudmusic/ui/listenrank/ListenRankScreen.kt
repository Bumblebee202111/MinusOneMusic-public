package com.github.bumblebee202111.minusonecloudmusic.ui.listenrank

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PlaylistPlayAllActions
import com.github.bumblebee202111.minusonecloudmusic.ui.common.SongWithPositionList
import com.github.bumblebee202111.minusonecloudmusic.ui.common.Toolbar
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme
import kotlinx.coroutines.launch

const val PLAY_RECORDS_TAB_INDEX_WEEK_DATA = 0
const val PLAY_RECORDS_TAB_INDEX_ALL_DATA = 1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListenRankScreen(
    userId: Long,
    onNavigateBack: () -> Unit,
    viewModel: ListenRankViewModel = hiltViewModel<ListenRankViewModel, ListenRankViewModel.Factory>(
        creationCallback = { factory -> factory.create(userId) }
    )
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    val tabs = listOf("最近一周", "所有时间")

    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        Toolbar(
            title = "听歌排行",
            onBackClick = onNavigateBack,
            iconRes = R.drawable.e54,
            iconTint = Color(0xFF283248)
        )

        SecondaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 46.dp)
                .height(30.dp),
            containerColor = Color.Transparent,
            divider = {},
            indicator = {
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(
                            selectedTabIndex = pagerState.currentPage,
                            matchContentSize = false
                        )
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.BottomCenter)
                        .offset(y = 3.dp)
                        .width(13.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(DolphinTheme.colors.primary1)
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                coroutineScope.launch { pagerState.animateScrollToPage(index) }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (selected) DolphinTheme.colors.text1 else DolphinTheme.colors.text4
                    )
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            ListenRankTabContent(
                tabIndex = page,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun ListenRankTabContent(
    tabIndex: Int,
    viewModel: ListenRankViewModel
) {
    val playRecordsFlow = remember(tabIndex) {
        when (tabIndex) {
            PLAY_RECORDS_TAB_INDEX_WEEK_DATA -> viewModel.weekRecordsUiState
            PLAY_RECORDS_TAB_INDEX_ALL_DATA -> viewModel.allRecordsUiState
            else -> throw IllegalArgumentException()
        }
    }
    val playRecords by playRecordsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        PlaylistPlayAllActions(
            count = playRecords?.size,
            onClick = {
                when (tabIndex) {
                    PLAY_RECORDS_TAB_INDEX_WEEK_DATA -> viewModel.playAllWeekRecords()
                    PLAY_RECORDS_TAB_INDEX_ALL_DATA -> viewModel.playAllAllRecords()
                }
            }
        )

        SongWithPositionList(
            songs = playRecords?:emptyList(),
            onItemClick = { position ->
                when (tabIndex) {
                    PLAY_RECORDS_TAB_INDEX_WEEK_DATA -> viewModel.onWeekRecordClick(position)
                    PLAY_RECORDS_TAB_INDEX_ALL_DATA -> viewModel.onAllRecordClick(position)
                }
            },
            modifier = Modifier.weight(1f)
        )
    }
}
