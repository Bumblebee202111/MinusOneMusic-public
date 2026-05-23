package com.github.bumblebee202111.minusonecloudmusic.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bumblebee202111.minusonecloudmusic.data.AppResult
import com.github.bumblebee202111.minusonecloudmusic.model.RemoteSong
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoggedInUserDataRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoginRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.PlaylistRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.SongRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.UserRepository
import com.github.bumblebee202111.minusonecloudmusic.ui.common.ToastManager
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import com.github.bumblebee202111.minusonecloudmusic.ui.mapper.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val loginRepository: LoginRepository,
    private val playlistRepository: PlaylistRepository,
    private val songRepository: SongRepository,
    private val loggedInUserDataRepository: LoggedInUserDataRepository,
    private val toastManager: ToastManager
) : ViewModel() {

    val loggedInUserId = loginRepository.loggedInUserId.stateInUi()

    val loggedInUserProfile = loggedInUserId.flatMapLatest { userId ->
        if (userId != null) {
            userRepository.getCachedUserProfile(userId)
        } else {
            flowOf(null)
        }
    }.stateInUi()

    init {
        registerAnonymousOrRefreshExisting()

        viewModelScope.launch {
            loginRepository.loggedInUserId.collect { userId ->
                refreshDataForLogin(userId)
            }
        }
    }

    fun registerAnonymousOrRefreshExisting() {
        viewModelScope.launch {
            if (loginRepository.isLoggedIn.first()) {
                val result = loginRepository.refreshLoginToken().first { it !is AppResult.Loading }
                if (result is AppResult.Error) {
                    toastManager.showMessage(result.error.toUiText())
                }
            } else if (!loginRepository.isLoggedInAsGuest.first()) {
                val result = loginRepository.registerAnonymous().first { it !is AppResult.Loading }
                if (result is AppResult.Error) {
                    toastManager.showMessage(result.error.toUiText())
                }
            }
        }
    }

    private fun refreshDataForLogin(userId: Long?) {
        viewModelScope.launch {
            if (userId != null) {
                val result = loggedInUserDataRepository.refreshMyLikedSongs()
                    .first { it !is AppResult.Loading }

                if (result is AppResult.Error) {
                    toastManager.showMessage(result.error.toUiText())
                }
            } else {
                loggedInUserDataRepository.clearMyLikedSongs()
            }
        }
        viewModelScope.launch {
            val playerPlaylistRemoteSongs =
                playlistRepository.playerPlaylistSongs().filterIsInstance<RemoteSong>()

            if (playerPlaylistRemoteSongs.isNotEmpty()) {
                val refreshResult = songRepository.refreshUserRemoteSongs(playerPlaylistRemoteSongs.map(RemoteSong::id))
                    .first { it !is AppResult.Loading }

                if (refreshResult is AppResult.Error) {
                    toastManager.showMessage(refreshResult.error.toUiText())
                }
            }
        }
    }

    fun onLogout() {
        viewModelScope.launch {
            val result = loginRepository.logout().first { it !is AppResult.Loading }
            if (result is AppResult.Error) {
                toastManager.showMessage(result.error.toUiText())
            }
        }
    }
}