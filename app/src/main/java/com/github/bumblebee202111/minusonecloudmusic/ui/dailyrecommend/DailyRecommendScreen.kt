package com.github.bumblebee202111.minusonecloudmusic.ui.dailyrecommend

import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PlaylistPlayAllActions
import com.github.bumblebee202111.minusonecloudmusic.ui.common.SongWithAlbumItem
import com.github.bumblebee202111.minusonecloudmusic.ui.common.Toolbar
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme


@Composable
fun DailyRecommendScreen(
    onNavigateBack: () -> Unit,
    viewModel: DailyRecommendViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val songs by viewModel.songItems.collectAsStateWithLifecycle(initialValue = emptyList())
    val bannerUrl by viewModel.banner.collectAsStateWithLifecycle(initialValue = null)
    val day = viewModel.day
    val month = viewModel.month

    val statusBarHeight = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
    val toolbarHeight = dimensionResource(id = R.dimen.toolbar_size)
    val pinnedHeight = toolbarHeight + statusBarHeight
    val headerMaxHeight = 178.dp

    val density = LocalDensity.current
    val maxOffsetPx = remember(density, headerMaxHeight, pinnedHeight) {
        with(density) { (headerMaxHeight - pinnedHeight).toPx() }
    }
    var offsetPx by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember(maxOffsetPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < 0) {
                    val newOffset = offsetPx + delta
                    val previousOffset = offsetPx
                    offsetPx = newOffset.coerceIn(-maxOffsetPx, 0f)
                    val consumed = offsetPx - previousOffset
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y
                if (delta > 0) {
                    val newOffset = offsetPx + delta
                    val previousOffset = offsetPx
                    offsetPx = newOffset.coerceIn(-maxOffsetPx, 0f)
                    val consumedY = offsetPx - previousOffset
                    return Offset(0f, consumedY)
                }
                return Offset.Zero
            }
        }
    }

    val bambooFontFamily = remember {
        FontFamily(Typeface.createFromAsset(context.assets, "bamboo.ttf"))
    }

    val shadowColor = colorResource(id = R.color.dm)
    val textShadow = remember(shadowColor) {
        Shadow(color = shadowColor, offset = Offset(0f, 2f), blurRadius = 4f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DolphinTheme.colors.backgroundAndroid)
            .nestedScroll(nestedScrollConnection)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerMaxHeight)
                .graphicsLayer {
                    translationY = offsetPx
                }
                .clipToBounds()
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(bannerUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationY = -offsetPx * 0.5f
                    }
            )
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, bottom = 20.dp),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = pinnedHeight)
                .graphicsLayer {
                    translationY = maxOffsetPx + offsetPx
                }
        ) {
            PlaylistPlayAllActions(
                count = null,
                onClick = viewModel::playAll,
                modifier = Modifier.background(
                    color = DolphinTheme.colors.backgroundAndroid,
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                )
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(songs ?: emptyList()) { index, song ->
                    SongWithAlbumItem(
                        song = song,
                        onClick = { viewModel.onSongItemClick(index) }
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = statusBarHeight)
        ) {
            Toolbar(
                title = "每日推荐",
                onBackClick = onNavigateBack,
                iconRes = R.drawable.aid,
                iconTint = Color.White,
                titleColor = Color.White
            )
        }
    }
}