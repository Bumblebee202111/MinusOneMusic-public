package com.github.bumblebee202111.minusonecloudmusic.ui.dailyrecommend

import com.github.bumblebee202111.minusonecloudmusic.coroutines.ApplicationScope
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.data.model.DailyRecommendSong
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoggedInUserDataRepository
import com.github.bumblebee202111.minusonecloudmusic.domain.GetPlaylistSongItemsUseCase
import com.github.bumblebee202111.minusonecloudmusic.domain.PlayPlaylistUseCase
import com.github.bumblebee202111.minusonecloudmusic.ui.common.AbstractRemotePlaylistViewModel
import com.github.bumblebee202111.minusonecloudmusic.utils.DateUtils
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class DailyRecommendViewModel @Inject constructor(
    loggedInUserDataRepository: LoggedInUserDataRepository,
    @ApplicationScope private val coroutineScope: CoroutineScope,
    private val playPlaylistUseCase: PlayPlaylistUseCase,
    private val getPlaylistSongItemsUseCase: GetPlaylistSongItemsUseCase,
    private val musicServiceConnection: MusicServiceConnection
) : AbstractRemotePlaylistViewModel<DailyRecommendSong>(playPlaylistUseCase) {

   private val songs=loggedInUserDataRepository.getDailyRecommendSongs().map { it.data }.stateInUi()

    val songUiModels =
        getPlaylistSongItemsUseCase(songs).stateInUi()

    override val loadedSongs: List<DailyRecommendSong>
        get() = songs.value?: emptyList()

    val player=musicServiceConnection.player.stateInUi()

    val banner =
        loggedInUserDataRepository.getDailyRecommendBanner().map { it.data }.stateInUi()

    val month=DateUtils.getCurrentMonthDisplayText()
    val day=DateUtils.getCurrentDayDisplayText()

}