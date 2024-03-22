package com.github.bumblebee202111.minusonecloudmusic.ui.dailyrecommend

import androidx.lifecycle.ViewModel
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoggedInUserDataRepository
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DailyRecommendViewModel @Inject constructor(loggedInUserDataRepository: LoggedInUserDataRepository) : ViewModel() {

    val dailyRecommendSongs=loggedInUserDataRepository.getDailyRecommendSongs().map { it.data }.flowOn(
        Dispatchers.IO)
        .stateInUi()
}