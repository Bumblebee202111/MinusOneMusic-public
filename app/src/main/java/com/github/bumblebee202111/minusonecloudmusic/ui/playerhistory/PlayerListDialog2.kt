package com.github.bumblebee202111.minusonecloudmusic.ui.playerhistory

import android.util.TypedValue
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.awaitNotLoading
import androidx.paging.compose.collectAsLazyPagingItems
import com.github.bumblebee202111.minusonecloudmusic.databinding.LayoutDialogPlayerList2Binding
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme
import kotlin.math.roundToInt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerListDialog2(
    onDismissRequest: () -> Unit,
    viewModel: PlayerHistoryViewModel = hiltViewModel()
) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val resources = LocalResources.current
    val dialogHeightDp = with(density) {
        (windowInfo.containerSize.height * 0.72f).toDp()
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = DolphinTheme.colors.backgroundWhite
    ) {
        AndroidViewBinding(
            factory = LayoutDialogPlayerList2Binding::inflate,
            modifier = Modifier
                .fillMaxWidth()
                .height(dialogHeightDp),
            update = {
                playlistSongs.apply {
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                    setContent {
                        DolphinTheme {
                            val songs = viewModel.songItemsPagingData.collectAsLazyPagingItems()
                            val listState = rememberLazyListState()
                            val currentSongPosition by viewModel.currentSongPosition.collectAsStateWithLifecycle(
                                initialValue = null
                            )

                            BoxWithConstraints {
                                val viewportHeight = constraints.maxHeight
                                val itemHeight = TypedValue.applyDimension(
                                    TypedValue.COMPLEX_UNIT_DIP,
                                    48F,
                                    resources.displayMetrics
                                ).roundToInt()

                                PagedPlayerSongList(
                                    songs = songs,
                                    onItemClick = { playlistSongItemUiModel, i ->
                                        viewModel.onItemClick(playlistSongItemUiModel.mediaId, i)
                                    },
                                    state = listState
                                )

                                LaunchedEffect(currentSongPosition) {
                                    snapshotFlow { songs.loadState }.awaitNotLoading()

                                    currentSongPosition?.let { index ->
                                        if (index < songs.itemCount) {
                                            val centerOffset = (viewportHeight / 2) - (itemHeight / 2)
                                            listState.scrollToItem(index, -centerOffset)
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
}