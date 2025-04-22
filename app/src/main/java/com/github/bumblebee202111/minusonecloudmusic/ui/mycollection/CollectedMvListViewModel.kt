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

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CollectedMvListViewModel  @Inject constructor(
    loginRepository: LoginRepository,
    private val loggedInUserDataRepository: LoggedInUserDataRepository
): ViewModel() {

    val myMvs=loginRepository.loggedInUserId.flatMapLatest { userId ->
        if(userId!=null){
            loggedInUserDataRepository.getMyMvs(20).map { it.data }
        }
        else{
           flowOf(null)
        }
    }.flowOn(Dispatchers.IO).stateInUi()
}