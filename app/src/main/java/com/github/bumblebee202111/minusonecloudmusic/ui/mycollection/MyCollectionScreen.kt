package com.github.bumblebee202111.minusonecloudmusic.ui.mycollection

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.github.bumblebee202111.minusonecloudmusic.databinding.LayoutMyCollectionHeaderBinding
import kotlinx.coroutines.launch
@Composable
fun MyCollectionScreen(
    onNavigateBack: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    val tabs = listOf("专辑", "MV")

    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        AndroidViewBinding(
            factory = LayoutMyCollectionHeaderBinding::inflate,
            update = {
                toolbar.setNavigationOnClickListener { onNavigateBack() }
            }
        )

        SecondaryTabRow(
            selectedTabIndex = pagerState.currentPage
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(title) }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> MyAlbumScreen()
                1 -> CollectedMvListScreen()
            }
        }
    }
}

