@file:OptIn(UnstableApi::class)

package com.github.bumblebee202111.minusonecloudmusic.ui.discover

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.media3.common.util.UnstableApi
import com.github.bumblebee202111.minusonecloudmusic.data.repository.DiscoverRepository
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.DeepLinkRegistry
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.NavigationManager
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    discoverRepository: DiscoverRepository,
    private val navigationManager: NavigationManager
) : ViewModel() {

    val blocks=discoverRepository.fetchDiscoverPage().stateInUi()

    fun onGenericUrlClick(url: String) {
        val navKey = DeepLinkRegistry.resolve(url)

        if (navKey != null) {
            navigationManager.navigate(navKey)
        } else {
        }
    }
}