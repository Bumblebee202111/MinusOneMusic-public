package com.github.bumblebee202111.minusonecloudmusic.ui.listenrank

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentListenRankTabBinding
import com.github.bumblebee202111.minusonecloudmusic.databinding.LayoutListenRankHeaderBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.SongWithPositionList
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

const val PLAY_RECORDS_TAB_INDEX_WEEK_DATA = 0
const val PLAY_RECORDS_TAB_INDEX_ALL_DATA = 1

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

    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        AndroidViewBinding(
            factory = LayoutListenRankHeaderBinding::inflate,
            update = {
                toolbar.setNavigationOnClickListener { onNavigateBack() }

                if (tabLayout.selectedTabPosition != pagerState.currentPage) {
                    tabLayout.getTabAt(pagerState.currentPage)?.select()
                }

                tabLayout.clearOnTabSelectedListeners()
                tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        tab?.let {
                            if (pagerState.currentPage != it.position) {
                                coroutineScope.launch { pagerState.animateScrollToPage(it.position) }
                            }
                        }
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {}
                    override fun onTabReselected(tab: TabLayout.Tab?) {}
                })
            }
        )

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

    AndroidViewBinding(
        factory = FragmentListenRankTabBinding::inflate,
        update = {
            this.playRecords = playRecords
            executePendingBindings()
            
            playlistActions.setOnClickListener {
                when (tabIndex) {
                    PLAY_RECORDS_TAB_INDEX_WEEK_DATA -> viewModel.playAllWeekRecords()
                    PLAY_RECORDS_TAB_INDEX_ALL_DATA -> viewModel.playAllAllRecords()
                }
            }
            
            list.apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    SongWithPositionList(
                        songs = playRecords ?: emptyList(),
                        onItemClick = { position ->
                            when (tabIndex) {
                                PLAY_RECORDS_TAB_INDEX_WEEK_DATA -> viewModel.onWeekRecordClick(position)
                                PLAY_RECORDS_TAB_INDEX_ALL_DATA -> viewModel.onAllRecordClick(position)
                            }
                        }
                    )
                }
            }
        }
    )
}
