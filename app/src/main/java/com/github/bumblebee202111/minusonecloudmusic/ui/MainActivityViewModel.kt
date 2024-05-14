package com.github.bumblebee202111.minusonecloudmusic.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bumblebee202111.minusonecloudmusic.data.datastore.PreferenceStorage
import com.github.bumblebee202111.minusonecloudmusic.data.model.LoggedInUser
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.PlaylistApiModel
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoggedInUserDataRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoginRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.UserRepository
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val preferenceStorage: PreferenceStorage,
    private val userRepository: UserRepository,
    private val loginRepository: LoginRepository,
    private val loggedInUserDataRepository: LoggedInUserDataRepository
) : ViewModel() {

    private val loggedInUserIdFlow = loginRepository.loggedInUserId


    val loggedInUserId = loginRepository.loggedInUserId.flowOn(Dispatchers.IO).stateInUi()

    val loggedInUserProfile = loggedInUserIdFlow.flatMapLatest { loggedInUserId ->
        if (loggedInUserId != null) {
            userRepository.getCachedUserProfile(loggedInUserId)
        } else
            flowOf(null)

    }.stateInUi()


    val user: StateFlow<LoggedInUser?> = MutableStateFlow(null)

    private val currentPlaylist: MutableStateFlow<PlaylistApiModel?> = MutableStateFlow(null)

    fun registerAnonymousOrRefreshExisting() {
        viewModelScope.launch(SupervisorJob() + Dispatchers.IO) {
            if (isLoggedIn()) {
                loginRepository.refreshLoginToken()
                refreshDataForLogin()
            } else if (!isLoggedInGuest()) {
                loginRepository.registerAnonymous().collect()
            }
        }
    }

    private suspend fun refreshDataForLogin() {
        loggedInUserDataRepository.refreshMyLikedSongs()
    }

    private suspend fun isLoggedIn() = preferenceStorage.currentLoggedInUserId.first() != null

    private suspend fun isLoggedInGuest() = preferenceStorage.currentAnonymousUserId.first() != null

    val currentSongMediaItems = currentPlaylist.map {

    }
}