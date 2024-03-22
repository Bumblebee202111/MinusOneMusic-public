package com.github.bumblebee202111.minusonecloudmusic.ui.discover

import androidx.lifecycle.ViewModel
import com.github.bumblebee202111.minusonecloudmusic.data.repository.PlaylistRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.PlaylistRepository.Companion.TOP_LIST_ID
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(private val playlistRepository: PlaylistRepository) : ViewModel() {
    val songs =
        playlistRepository.getPlaylistDetail(TOP_LIST_ID).map { it.data?.songs?.take(3) }.flowOn(Dispatchers.IO)
            .stateInUi()

}