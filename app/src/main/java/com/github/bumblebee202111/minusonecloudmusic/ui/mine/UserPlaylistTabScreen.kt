package com.github.bumblebee202111.minusonecloudmusic.ui.mine

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.ui.LocalMiniPlayerBarHeight
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme
import com.github.bumblebee202111.minusonecloudmusic.utils.imageUrl

@Composable
fun UserPlaylistTabScreen(
    viewModel: MineViewModel,
    category: UserPlaylistTab,
    onItemClick: (UserPlaylistItem) -> Unit
) {
    val allTabsData by viewModel.myPlaylistTabs.collectAsStateWithLifecycle(initialValue = null)
    val items = allTabsData?.get(category) ?: emptyList()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = LocalMiniPlayerBarHeight.current + 28.dp)
    ) {
        items(
            items = items,
            key = { item ->
                when (item) {
                    is NormalPlaylistItem -> item.playlist.id
                    is UserChartsItem -> "charts_${item.userId}"
                }
            }
        ) { item ->
            when (item) {
                is NormalPlaylistItem -> {
                    NormalPlaylistItemView(item) { onItemClick(item) }
                }

                is UserChartsItem -> {
                    UserChartsItemView(item) { onItemClick(item) }
                }
            }
        }
    }
}

@Composable
private fun NormalPlaylistItemView(
    item: NormalPlaylistItem,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val sizePx = remember(density) { with(density) { 52.dp.roundToPx() } }
    val placeholderPainter = remember(context, sizePx) {
        val drawable = ContextCompat.getDrawable(context, R.drawable.dnl)
        drawable?.toBitmap(width = sizePx, height = sizePx)?.asImageBitmap()
            ?.let { BitmapPainter(it) }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.playlist.coverImgUrl.imageUrl(thumbnailSize = 116, quality = 80),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = placeholderPainter,
            error = placeholderPainter,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(DolphinTheme.colors.text6_1)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = item.playlist.name,
                color = DolphinTheme.colors.text1,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "${item.playlist.trackCount}首·${item.playlist.playCount}次播放",
                color = DolphinTheme.colors.text4,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun UserChartsItemView(
    item: UserChartsItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.icn_my_music_listen_rank),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(DolphinTheme.colors.text6_1)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.charts),
                color = DolphinTheme.colors.text1,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = stringResource(id = R.string.hz2, item.listenSongs),
                color = DolphinTheme.colors.text4,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}