package com.github.bumblebee202111.minusonecloudmusic.ui.comments

import androidx.lifecycle.ViewModel
import com.github.bumblebee202111.minusonecloudmusic.data.repository.SongRepository
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map

@HiltViewModel(assistedFactory = CommentsViewModel.Factory::class)
class CommentsViewModel @AssistedInject constructor(
    @Assisted val threadId: String,
    songRepository: SongRepository
) : ViewModel() {

    val comments = songRepository.getComments(threadId).map { it.data }.stateInUi()

    @AssistedFactory
    interface Factory {
        fun create(threadId: String): CommentsViewModel
    }
}