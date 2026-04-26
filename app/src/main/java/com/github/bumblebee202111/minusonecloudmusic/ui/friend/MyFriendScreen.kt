package com.github.bumblebee202111.minusonecloudmusic.ui.friend

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bumblebee202111.minusonecloudmusic.R
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFriendScreen(
    onNavigateBack: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    val tabs = listOf("关注", "粉丝")

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(painterResource(R.drawable.aid), contentDescription = "Back")
                }
            }
        )
        PrimaryTabRow (
            selectedTabIndex = pagerState.currentPage,
            tabs = {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(title) }
                    )
                }
            })
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> FollowScreen()
                1 -> FansScreen()
            }
        }
    }
}
@Composable
fun FollowScreen(
    viewModel: FollowViewModel = hiltViewModel()
) {
    val follows by viewModel.userFollows.collectAsStateWithLifecycle(initialValue = emptyList())
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(follows ?: emptyList()) { user ->
            Text(
                text = user.displayName,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}
@Composable
fun FansScreen(
    viewModel: FansViewModel = hiltViewModel()
) {
    val fans by viewModel.userFans.collectAsStateWithLifecycle(initialValue = emptyList())
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(fans ?: emptyList()) { user ->
            Text(
                text = user.displayName,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}
