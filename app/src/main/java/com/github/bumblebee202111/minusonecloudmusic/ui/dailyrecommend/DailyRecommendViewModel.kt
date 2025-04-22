package com.github.bumblebee202111.minusonecloudmusic.ui.dailyrecommend

import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.data.model.DailyRecommendSong
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoggedInUserDataRepository
import com.github.bumblebee202111.minusonecloudmusic.domain.MapSongsFlowToUiItemsUseCase
import com.github.bumblebee202111.minusonecloudmusic.domain.PlayPlaylistUseCase
import com.github.bumblebee202111.minusonecloudmusic.ui.common.AbstractPlaylistViewModel
import com.github.bumblebee202111.minusonecloudmusic.utils.DateUtils
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DailyRecommendViewModel @Inject constructor(
    loggedInUserDataRepository: LoggedInUserDataRepository,
    playPlaylistUseCase: PlayPlaylistUseCase,
    mapSongsFlowToUiItemsUseCase: MapSongsFlowToUiItemsUseCase,
    musicServiceConnection: MusicServiceConnection
) : AbstractPlaylistViewModel<DailyRecommendSong>(playPlaylistUseCase) {

   private val songs=loggedInUserDataRepository.getDailyRecommendSongs().map { it.data }.stateInUi()

    val songItems =
        mapSongsFlowToUiItemsUseCase(songs).stateInUi()

    override val loadedSongs: List<DailyRecommendSong>
        get() = songs.value?: emptyList()

    val player = musicServiceConnection.player

    val banner =
        loggedInUserDataRepository.getDailyRecommendBanner().map { it.data }.stateInUi()

    val month=DateUtils.getCurrentMonthDisplayText()
    val day=DateUtils.getCurrentDayDisplayText()

}