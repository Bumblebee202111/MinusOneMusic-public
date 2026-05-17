package com.github.bumblebee202111.minusonecloudmusic.ui.playlist

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.asDrawable
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.databinding.ViewPlaylistBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PagedSongWithPositionList
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PlaylistScreenUIHelper
import com.github.bumblebee202111.minusonecloudmusic.ui.common.applyDominantColor
import com.github.bumblebee202111.minusonecloudmusic.ui.common.setBackgroundColorAndTopCorner
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme
import com.github.bumblebee202111.minusonecloudmusic.utils.imageUrl
import kotlin.math.abs
import kotlin.math.min

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
    val context = LocalContext.current
    val resources = LocalResources.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val songs = viewModel.playlistSongs.collectAsLazyPagingItems()
    val playlistDetail by viewModel.playlistDetail.collectAsStateWithLifecycle(initialValue = null)

    AndroidViewBinding(
        factory = ViewPlaylistBinding::inflate,
        modifier = Modifier.fillMaxSize(),
        update = {
            this.lifecycleOwner = lifecycleOwner
            this.viewModel = viewModel

            ViewCompat.setOnApplyWindowInsetsListener(appBarLayout) { _, insets ->
                val topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
                val toolbarSize = root.resources.getDimensionPixelSize(R.dimen.toolbar_size)
                (toolbar.layoutParams as ViewGroup.MarginLayoutParams).topMargin = topInset

                (this.playlistDetail.layoutParams as ViewGroup.MarginLayoutParams).topMargin = topInset + toolbarSize

                toolbarBackground.run {
                    layoutParams.height = toolbarSize + topInset
                    isVisible = true
                    requestLayout()
                }
                WindowInsetsCompat.CONSUMED
            }

            toolbar.setNavigationOnClickListener { onNavigateBack() }

        appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange
            val fraction = min(abs(verticalOffset).toFloat() / totalScrollRange, 0.3F)
            val timeInterpolator = FastOutSlowInInterpolator()
            val interpolation = timeInterpolator.getInterpolation(fraction)
            val alpha = 0.0F + interpolation * (0.3F - 0.0F)
            toolbarBackground.alpha = alpha
        }

            val playlistActionsView = root.findViewById<View>(R.id.playlist_actions)
            playlistActionsView?.setBackgroundColorAndTopCorner(R.color.colorBackgroundAndroid, 12F)

            PlaylistScreenUIHelper(
                view = root,
                playAllAction = viewModel::playAll
            )

            songList.apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    DolphinTheme {
                        PagedSongWithPositionList(
                            songs = songs,
                            onItemClick = viewModel::onSongItemClick
                        )
                    }
                }
            }

            this.playlistDetail.apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    DolphinTheme {
                        playlistDetail?.let { detail ->
                            Row(
                                modifier = Modifier.padding(bottom = 16.dp)
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

                                    val request = ImageRequest.Builder(context)
                                        .data(detail.coverImgUrl.imageUrl(thumbnailSize = 200, quality = 80))
                                        .allowHardware(false)
                                        .listener(
                                            onSuccess = { _, result ->
                                                applyDominantColor(
                                                    drawable = result.image.asDrawable(resources),
                                                    targetView = gradientBg,
                                                    defaultColor = Color.BLACK,
                                                    minL = 0.45F,
                                                    maxL = 0.75F
                                                )
                                            },
                                            onError = { _, _ ->
                                                gradientBg.setBackgroundColor(Color.BLACK)
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

                                Column(
                                    modifier = Modifier
                                        .padding(start = 10.dp, top = 13.dp)
                                        .weight(1f)
                                ) {
                                    Text(
                                        text = detail.name,
                                        color = androidx.compose.ui.graphics.Color.White,
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
                                            model = detail.creator?.avatarUrl?.imageUrl(thumbnailSize = 100, quality = 80),
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
                                            text = detail.creator?.displayName ?: "",
                                            color = androidx.compose.ui.graphics.Color(0xB3FFFFFF),
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
                    }
                }
            }
        }
    )
}

const val PLAYLIST_CREATOR_ID_UNKNOWN = -1L