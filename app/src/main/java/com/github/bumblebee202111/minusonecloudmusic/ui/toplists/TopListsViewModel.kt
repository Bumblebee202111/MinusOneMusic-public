package com.github.bumblebee202111.minusonecloudmusic.ui.toplists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bumblebee202111.minusonecloudmusic.data.repository.PlaylistRepository
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TopListsViewModel @Inject constructor(playlistRepository: PlaylistRepository) : ViewModel() {
    val topLists=playlistRepository.getTopLists().map { it.data }.stateIn(viewModelScope, SharingStarted.Lazily,null).stateInUi()
}