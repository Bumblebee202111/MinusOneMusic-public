package com.github.bumblebee202111.minusonecloudmusic.ui.playlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.asDrawable
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.model.PlaylistDetail
import com.github.bumblebee202111.minusonecloudmusic.ui.common.ColorUtils
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PagedSongWithPositionList
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PlaylistPlayAllActions
import com.github.bumblebee202111.minusonecloudmusic.ui.common.Toolbar
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme
import com.github.bumblebee202111.minusonecloudmusic.utils.imageUrl
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun PlaylistScreen(
    playlistId: Long,
    creatorId: Long? = null,
    isMyPL: Boolean? = null,
    isV6: Boolean? = null,
    onNavigateBack: () -> Unit,
    viewModel: PlaylistViewModel = hiltViewModel<PlaylistViewModel, PlaylistViewModel.Factory>(
        creationCallback = { factory -> factory.create(playlistId, creatorId, isMyPL, isV6) }
    )
) {
    val songs = viewModel.playlistSongs.collectAsLazyPagingItems()
    val playlistDetail by viewModel.playlistDetail.collectAsStateWithLifecycle(initialValue = null)

    val density = LocalDensity.current
    val insets = WindowInsets.systemBars
    val topInsetPx = with(density) { insets.getTop(density).toFloat() }
    val topInset = with(density) { topInsetPx.toDp() }

    val toolbarHeight = dimensionResource(id = R.dimen.toolbar_size)
    val toolbarHeightPx = with(density) { toolbarHeight.toPx() }

    var headerHeightPx by remember { mutableFloatStateOf(0f) }
    var offsetPx by remember { mutableFloatStateOf(0f) }

    val minOffsetPx = if (headerHeightPx > 0) -(headerHeightPx - (toolbarHeightPx + topInsetPx)) else 0f
    val pinnedHeightPx = toolbarHeightPx + topInsetPx + with(density) { 50.dp.toPx() }

    val nestedScrollConnection = remember(minOffsetPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < 0 && minOffsetPx < 0) {
                    val newOffset = offsetPx + delta
                    val previousOffset = offsetPx
                    offsetPx = newOffset.coerceIn(minOffsetPx, 0f)
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
                if (delta > 0 && minOffsetPx < 0) {
                    val newOffset = offsetPx + delta
                    val previousOffset = offsetPx
                    offsetPx = newOffset.coerceIn(minOffsetPx, 0f)
                    val consumedY = offsetPx - previousOffset
                    return Offset(0f, consumedY)
                }
                return Offset.Zero
            }
        }
    }

    DolphinTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.colorBackgroundAndroid))
                .nestedScroll(nestedScrollConnection)
        ) {
            PagedSongWithPositionList(
                songs = songs,
                onItemClick = viewModel::onSongItemClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .layout { measurable, constraints ->
                        val listHeight = constraints.maxHeight - pinnedHeightPx.roundToInt()
                        val placeable = measurable.measure(
                            constraints.copy(
                                minHeight = listHeight,
                                maxHeight = listHeight
                            )
                        )
                        layout(placeable.width, placeable.height) {
                            val yPosition = (headerHeightPx + with(density) { 50.dp.toPx() } + offsetPx).roundToInt()
                            placeable.place(0, yPosition)
                        }
                    }
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        layout(placeable.width, placeable.height) {
                            placeable.place(0, offsetPx.roundToInt())
                        }
                    }
            ) {
                var dominantColor by remember { mutableIntStateOf(android.graphics.Color.BLACK) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(with(density) { headerHeightPx.toDp() } + 50.dp)
                        .background(Color(dominantColor).copy(alpha = 0.6f))
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                headerHeightPx = coordinates.size.height.toFloat()
                            }
                    ) {
                        PlaylistDetailContent(
                            detail = playlistDetail,
                            topInset = topInset,
                            toolbarHeight = toolbarHeight,
                            onColorGenerated = { dominantColor = it }
                        )
                    }

                    PlaylistPlayAllActions(
                        count = playlistDetail?.songCount,
                        onClick = viewModel::playAll,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(
                                color = colorResource(id = R.color.colorBackgroundAndroid),
                                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                            )
                    )
                }
            }
            val totalScrollRange = abs(minOffsetPx)
            val fraction = if (totalScrollRange > 0) {
                min(abs(offsetPx) / totalScrollRange, 0.3f)
            } else 0f
            val timeInterpolator = remember { FastOutSlowInInterpolator() }
            val interpolation = timeInterpolator.getInterpolation(fraction)
            val toolbarAlpha = interpolation * 0.3f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(toolbarHeight + topInset)
                    .background(colorResource(id = R.color.colorBackgroundAndroid).copy(alpha = toolbarAlpha))
            )
            Toolbar(
                title = playlistDetail?.name ?: "",
                onBackClick = onNavigateBack,
                modifier = Modifier.padding(top = topInset),
                iconTint = Color.White,
                titleColor = Color.White
            )
        }
    }
}

@Composable
private fun PlaylistDetailContent(
    detail: PlaylistDetail?,
    topInset: Dp,
    toolbarHeight: Dp,
    onColorGenerated: (Int) -> Unit
) {
    val context = LocalContext.current
    val appContext = context.applicationContext

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = topInset + toolbarHeight, bottom = 16.dp)
    ) {
        Box(
            modifier = Modifier.size(width = 100.dp, height = 113.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.hg8),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .size(100.dp)
                    .padding(top = 13.dp)
            )

            if (detail != null) {
                val request = ImageRequest.Builder(context)
                    .data(detail.coverImgUrl.imageUrl(thumbnailSize = 200, quality = 80))
                    .allowHardware(false)
                    .listener(
                        onSuccess = { _, result ->
                            ColorUtils.getDominantColor(
                                drawable = result.image.asDrawable(appContext.resources),
                                defaultColor = android.graphics.Color.BLACK,
                                minL = 0.45F,
                                maxL = 0.75F,
                                onGenerated = onColorGenerated
                            )
                        },
                        onError = { _, _ ->
                            onColorGenerated(android.graphics.Color.BLACK)
                        }
                    )
                    .build()
                AsyncImage(
                    model = request,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.h_1),
                    error = painterResource(id = R.drawable.h_1),
                    modifier = Modifier
                        .padding(top = 13.dp, bottom = 8.dp)
                        .size(100.dp)
                        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.rt)))
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(start = 10.dp, top = 13.dp)
                .weight(1f)
        ) {
            Text(
                text = detail?.name ?: "",
                color = Color.White,
                fontSize = 16.sp,
                lineHeight = (16 * 1.2).sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 2.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 6.dp)
            ) {
                AsyncImage(
                    model = detail?.creator?.avatarUrl?.imageUrl(thumbnailSize = 100, quality = 80),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.fgw),
                    error = painterResource(id = R.drawable.fgw),
                    modifier = Modifier
                        .size(34.dp)
                        .padding(5.dp)
                        .clip(CircleShape)
                )

                Text(
                    text = detail?.creator?.displayName ?: "",
                    color = Color(0xB3FFFFFF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(start = 3.dp)
                        .alpha(0.6f)
                )
            }
        }
    }
}

const val PLAYLIST_CREATOR_ID_UNKNOWN = -1L