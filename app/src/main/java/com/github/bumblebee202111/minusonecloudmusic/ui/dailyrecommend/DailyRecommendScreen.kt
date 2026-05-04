package com.github.bumblebee202111.minusonecloudmusic.ui.dailyrecommend

import android.graphics.Typeface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bumblebee202111.minusonecloudmusic.databinding.ViewDailyRecommendBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PlaylistFragmentUIHelper
import com.github.bumblebee202111.minusonecloudmusic.ui.common.SongWithAlbumList
import com.github.bumblebee202111.minusonecloudmusic.ui.common.loadImage


@Composable
fun DailyRecommendScreen(
    onNavigateBack: () -> Unit,
    viewModel: DailyRecommendViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val songs by viewModel.songItems.collectAsStateWithLifecycle(initialValue = emptyList())
    val bannerUrl by viewModel.banner.collectAsStateWithLifecycle(initialValue = null)

    AndroidViewBinding(
        factory = ViewDailyRecommendBinding::inflate,
        modifier = Modifier.fillMaxSize(),
        update = {
            this.viewModel = viewModel
            this.lifecycleOwner = lifecycleOwner

            toolbar.setNavigationOnClickListener { onNavigateBack() }

            val typeface = Typeface.createFromAsset(context.assets, "bamboo.ttf")
            tvPendantDayRecommendDateInfo.typeface = typeface
            tvPendantMonthText.typeface = typeface

            PlaylistFragmentUIHelper(
                view = this.root,
                playAllAction = viewModel::playAll
            )

            background.loadImage(bannerUrl)

            dailyRecommendList.setContent {
                SongWithAlbumList(
                    songs = songs ?: emptyList(),
                    onItemClick = viewModel::onSongItemClick
                )
            }
        }
    )
}