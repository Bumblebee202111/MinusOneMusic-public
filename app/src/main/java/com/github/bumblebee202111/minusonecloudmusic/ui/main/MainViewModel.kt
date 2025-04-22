package com.github.bumblebee202111.minusonecloudmusic.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val musicServiceConnection: MusicServiceConnection,private val loginRepository: LoginRepository):
    ViewModel() {

    fun isLoggedIn()=loginRepository.isLoggedIn

    fun onLogout() {
        viewModelScope.launch {
            loginRepository.logout()
        }
    }

    val player = musicServiceConnection.player
}