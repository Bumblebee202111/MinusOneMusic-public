package com.github.bumblebee202111.minusonecloudmusic.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bumblebee202111.minusonecloudmusic.data.AppResult
import com.github.bumblebee202111.minusonecloudmusic.model.LoggedInUser
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
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

    val loggedInUserId = loginRepository.loggedInUserId.onEach {
        refreshDataForLogin(it)
    }.flowOn(Dispatchers.IO).stateInUi()

    val loggedInUserProfile = this.loggedInUserId.flatMapLatest { loggedInUserId ->
        if (loggedInUserId != null) {
            userRepository.getCachedUserProfile(loggedInUserId)
        } else
            flowOf(null)
    }.stateInUi()

    val user: StateFlow<LoggedInUser?> = MutableStateFlow(null)

    fun registerAnonymousOrRefreshExisting() {
        viewModelScope.launch(SupervisorJob() + Dispatchers.IO) {
            if (loginRepository.isLoggedIn.first()) {
                val result = loginRepository.refreshLoginToken().first()
                if (result is AppResult.Error) {
                    toastManager.showMessage(result.error.toUiText())
                }
            } else if (!loginRepository.isLoggedInAsGuest.first()) {
                loginRepository.registerAnonymous().first()
            }
        }
    }

    private suspend fun refreshDataForLogin(userId: Long?) {
        if (userId != null) {
            loggedInUserDataRepository.refreshMyLikedSongs().collect {

            }
        } else {
            loggedInUserDataRepository.clearMyLikedSongs()
        }
        val playerPlaylistRemoteSongs =
            playlistRepository.playerPlaylistSongs().filterIsInstance<RemoteSong>()
        val refreshUserRemoteSongsResult =
            songRepository.refreshUserRemoteSongs(playerPlaylistRemoteSongs.map(RemoteSong::id))
                .first { it !is AppResult.Loading }
        if (refreshUserRemoteSongsResult is AppResult.Error) {
            toastManager.showMessage(refreshUserRemoteSongsResult.error.toUiText())
        }
    }

    fun onLogout() {
        viewModelScope.launch {
            loginRepository.logout().collect { result ->
                if (result is AppResult.Error) {
                    toastManager.showMessage(result.error.toUiText())
                }
            }
        }
    }

}