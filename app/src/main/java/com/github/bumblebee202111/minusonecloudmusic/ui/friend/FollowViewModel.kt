package com.github.bumblebee202111.minusonecloudmusic.ui.friend

import androidx.lifecycle.ViewModel
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoginRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.UserRepository
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class FollowViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    val userFollows=loginRepository.loggedInUserId.flatMapLatest { userId ->
        if(userId!=null){
            userRepository.getUserFollows(userId).map { it.data }
        }
        else{
           flowOf(null)
        }
    }.flowOn(Dispatchers.IO).stateInUi()
}