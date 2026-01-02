package com.github.bumblebee202111.minusonecloudmusic.ui.comments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.github.bumblebee202111.minusonecloudmusic.data.repository.SongRepository
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class CommentsViewModel @Inject constructor(
    songRepository: SongRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val threadId = CommentsFragmentArgs.fromSavedStateHandle(savedStateHandle).threadId

    val comments = songRepository.getComments(threadId).map { it.data }.stateInUi()
}