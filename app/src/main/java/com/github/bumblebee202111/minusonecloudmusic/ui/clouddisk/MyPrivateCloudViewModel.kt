package com.github.bumblebee202111.minusonecloudmusic.ui.clouddisk

import androidx.lifecycle.ViewModel
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoggedInUserDataRepository
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MyPrivateCloudViewModel @Inject constructor(private val loggedInUserDataRepository: LoggedInUserDataRepository) : ViewModel() {

    val cloudSongs =
        loggedInUserDataRepository.getCloudSongs().map { it.data }.flowOn(Dispatchers.IO).stateInUi()
}