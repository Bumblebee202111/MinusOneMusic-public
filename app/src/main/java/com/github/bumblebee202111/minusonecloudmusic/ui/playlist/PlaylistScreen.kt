package com.github.bumblebee202111.minusonecloudmusic.ui.playlist

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.load
import coil3.request.error
import coil3.request.placeholder
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.databinding.ViewPlaylistBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PagedSongWithPositionList
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PlaylistScreenUIHelper
import com.github.bumblebee202111.minusonecloudmusic.ui.common.applyDominantColor
import com.github.bumblebee202111.minusonecloudmusic.ui.common.loadImage
import com.github.bumblebee202111.minusonecloudmusic.ui.common.setBackgroundColorAndTopCorner
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

            songList.setContent {
                PagedSongWithPositionList(
                    songs = songs,
                    onItemClick = viewModel::onSongItemClick
                )
            }

            playlistDetail?.let { detail ->
                creatorAvatar.loadImage(detail.creator?.avatarUrl, circleCrop = true)

                val coverUrl = detail.coverImgUrl
                playlistCover.load(coverUrl) {
                    placeholder(R.drawable.h_1)
                    error(R.drawable.h_1)
                    listener(
                        onSuccess = { _, result ->
                            applyDominantColor(
                                result = result,
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
                }
            }
        }
    )
}

const val PLAYLIST_CREATOR_ID_UNKNOWN = -1L