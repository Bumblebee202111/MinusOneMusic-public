package com.github.bumblebee202111.minusonecloudmusic.ui.dailyrecommend

import android.graphics.Typeface
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.databinding.ViewDailyRecommendBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PlaylistScreenUIHelper
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

            PlaylistScreenUIHelper(
                view = this.root,
                playAllAction = viewModel::playAll
            )

            background.loadImage(bannerUrl)

            dateContainerCompose.apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    val day = viewModel.day
                    val month = viewModel.month

                    val bambooFontFamily = remember {
                        FontFamily(Typeface.createFromAsset(context.assets, "bamboo.ttf"))
                    }

                    val shadowColor = colorResource(id = R.color.dm)
                    val textShadow = remember(shadowColor) {
                        Shadow(
                            color = shadowColor,
                            offset = Offset(0f, 2f),
                            blurRadius = 4f
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = day,
                            color = Color.White,
                            fontSize = 40.sp,
                            fontFamily = bambooFontFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(shadow = textShadow),
                            modifier = Modifier.alignByBaseline()
                        )
                        Text(
                            text = "/",
                            color = Color.White,
                            fontSize = 15.sp,
                            style = TextStyle(shadow = textShadow),
                            modifier = Modifier
                                .padding(start = 2.dp)
                                .alignByBaseline()
                        )
                        Text(
                            text = month,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontFamily = bambooFontFamily,
                            style = TextStyle(shadow = textShadow),
                            modifier = Modifier
                                .padding(start = 2.dp)
                                .alignByBaseline()
                        )
                    }
                }
            }

            dailyRecommendList.setContent {
                SongWithAlbumList(
                    songs = songs ?: emptyList(),
                    onItemClick = viewModel::onSongItemClick
                )
            }
        }
    )
}