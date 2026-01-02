@file:OptIn(UnstableApi::class)

package com.github.bumblebee202111.minusonecloudmusic.ui.discover

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.media3.common.util.UnstableApi
import com.github.bumblebee202111.minusonecloudmusic.data.repository.DiscoverRepository
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    discoverRepository: DiscoverRepository
) : ViewModel() {
    val blocks = discoverRepository.getDiscoverBlocks().map { it.data }.stateInUi(emptyList())
}