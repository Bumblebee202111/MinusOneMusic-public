package com.github.bumblebee202111.minusonecloudmusic.ui.playerhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.Util
import androidx.paging.cachedIn
import com.github.bumblebee202111.minusonecloudmusic.coroutines.AppDispatchers
import com.github.bumblebee202111.minusonecloudmusic.coroutines.Dispatcher
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.data.repository.PlaylistRepository
import com.github.bumblebee202111.minusonecloudmusic.domain.MapSongPagingDataFlowToUiItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PlayerHistoryViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection,
    mapSongPagingDataFlowToUiItemsUseCase: MapSongPagingDataFlowToUiItemsUseCase,
    private val playlistRepository: PlaylistRepository,
    @Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    suspend fun getCurrentSongPosition() = withContext(ioDispatcher) {
        musicServiceConnection.currentMediaId.first()?.let {
            playlistRepository.getPlayerPlaylistSongPosition(it)
        }
    }

    val songItemsPagingData =
        mapSongPagingDataFlowToUiItemsUseCase(
            playlistRepository.getPlayerPlaylistPagingData().cachedIn(viewModelScope),
        ).cachedIn(viewModelScope)

    fun onItemClick(mediaId: String, songItemPosition: Int) {
        val player = musicServiceConnection.player.value ?: return
        val currentMediaId = musicServiceConnection.currentMediaId.value
        if (mediaId != currentMediaId) {
            viewModelScope.launch {
                with(player) {
                    if (isCommandAvailable(Player.COMMAND_SET_MEDIA_ITEM))
                        seekTo(songItemPosition, C.TIME_UNSET)
                    Util.handlePlayButtonAction(this)
                }

            }
        }
    }


}