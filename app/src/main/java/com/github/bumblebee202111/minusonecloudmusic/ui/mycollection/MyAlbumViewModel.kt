@file:OptIn(ExperimentalCoroutinesApi::class)

package com.github.bumblebee202111.minusonecloudmusic.ui.mycollection

import androidx.lifecycle.ViewModel
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoggedInUserDataRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoginRepository
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MyAlbumViewModel @Inject constructor(
    loginRepository: LoginRepository,
    loggedInUserDataRepository: LoggedInUserDataRepository
) : ViewModel() {
    val myAlbums = loginRepository.loggedInUserId.flatMapLatest { userId ->
        if (userId != null) {
            loggedInUserDataRepository.getMyAlbums(20, 0).map { it.data }
        } else {
            flowOf(null)
        }
    }.flowOn(Dispatchers.IO).stateInUi()
}