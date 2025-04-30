package com.github.bumblebee202111.minusonecloudmusic.ui.dailyrecommend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoggedInUserDataRepository
import com.github.bumblebee202111.minusonecloudmusic.domain.MapSongsFlowToUiItemsUseCase
import com.github.bumblebee202111.minusonecloudmusic.domain.PlayPlaylistUseCase
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PlaylistPlaybackHandler
import com.github.bumblebee202111.minusonecloudmusic.utils.DateUtils
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DailyRecommendViewModel @Inject constructor(
    loggedInUserDataRepository: LoggedInUserDataRepository,
    playPlaylistUseCase: PlayPlaylistUseCase,
    mapSongsFlowToUiItemsUseCase: MapSongsFlowToUiItemsUseCase
) :ViewModel() {

   private val songs=loggedInUserDataRepository.getDailyRecommendSongs().map { it.data }.stateInUi()

    val songItems =
        mapSongsFlowToUiItemsUseCase(songs).stateInUi()

    private val playbackHandler = PlaylistPlaybackHandler(
        playPlaylistUseCase = playPlaylistUseCase,
        scope = viewModelScope,
        getLoadedSongs = { songs.value ?: emptyList() },
        loadRemainingSongs = null
    )

    fun onSongItemClick(startIndex: Int) = playbackHandler.onSongItemClick(startIndex)
    fun playAll() = playbackHandler.playAll()

    val banner =
        loggedInUserDataRepository.getDailyRecommendBanner().map { it.data }.stateInUi()

    val month=DateUtils.getCurrentMonthDisplayText()
    val day=DateUtils.getCurrentDayDisplayText()

}