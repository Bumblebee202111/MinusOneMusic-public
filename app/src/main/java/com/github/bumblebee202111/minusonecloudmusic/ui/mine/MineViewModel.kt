package com.github.bumblebee202111.minusonecloudmusic.ui.mine

import androidx.lifecycle.ViewModel
import com.github.bumblebee202111.minusonecloudmusic.data.model.SpecialType
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoginRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.UserRepository
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MineViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val loggedInUserIdFlow = loginRepository.loggedInUserId

    private val myIdAndUserDetailFlow = loggedInUserIdFlow.flatMapLatest { userId ->
        if (userId != null) {
            userRepository.getUserDetail(userId, true).map {
                Pair(userId, it.data)
            }
        } else {
            flowOf(null)
        }
    }

    val myProfile = loggedInUserIdFlow.flatMapLatest { userId ->
        if (userId != null) {
            userRepository.getCachedUserProfile(userId)
        } else {
            flowOf(null)
        }
    }.stateInUi()
    private val myPlaylistsFlow =
        loginRepository.loggedInUserId.flatMapLatest { userId ->
            if (userId != null) {
                userRepository.getUserPlaylists(userId).map {
                    it.data
                }
            } else {
                flowOf(null)
            }
        }

    val myPlaylistTabs = combine(
        myIdAndUserDetailFlow,
        myPlaylistsFlow
    ) { myIdAndUserDetail, myPlaylists ->
        if (myIdAndUserDetail == null || myPlaylists == null)
            return@combine null
        val myUserDetail = myIdAndUserDetail.second ?: return@combine null
        val myId = myIdAndUserDetail.first

        val myPlaylistItems = mutableListOf<UserPlaylistItem>()

        myPlaylistItems += UserChartsItem(
            myId,
            myUserDetail.listenSongs
        )

        myPlaylistItems += myPlaylists.map { NormalPlaylistItem(it) }
        myPlaylistItems.find { it is NormalPlaylistItem && it.playlist.specialType == SpecialType.STAR && it.playlist.creatorId == myId }
            ?.let {
                myPlaylistItems.remove(it)
                myPlaylistItems.add(0, it)
            }

         myPlaylistItems.groupBy {
            when (it) {
                is NormalPlaylistItem -> {
                    if (it.playlist.creatorId == myId)
                        UserPlaylistTab.CREATED
                    else
                        UserPlaylistTab.COLLECTED
                }

                is UserChartsItem -> {
                    UserPlaylistTab.CREATED
                }
            }
        }
    }.flowOn(Dispatchers.IO).stateInUi()

}