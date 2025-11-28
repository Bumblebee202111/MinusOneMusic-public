package com.github.bumblebee202111.minusonecloudmusic.ui.search

import androidx.lifecycle.ViewModel
import com.github.bumblebee202111.minusonecloudmusic.coroutines.AppDispatchers
import com.github.bumblebee202111.minusonecloudmusic.coroutines.ApplicationScope
import com.github.bumblebee202111.minusonecloudmusic.coroutines.Dispatcher
import com.github.bumblebee202111.minusonecloudmusic.data.repository.SearchRepository
import com.github.bumblebee202111.minusonecloudmusic.domain.MapSongsFlowToUiItemsUseCase
import com.github.bumblebee202111.minusonecloudmusic.domain.PlayPlaylistUseCase
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    mapSongsFlowToUiItemsUseCase: MapSongsFlowToUiItemsUseCase,
    private val playPlaylistUseCase: PlayPlaylistUseCase,
    @ApplicationScope private val applicationScope: CoroutineScope,
    @Dispatcher(AppDispatchers.Main) private val mainDispatcher: CoroutineDispatcher
) : ViewModel() {
    private val keyword = MutableStateFlow<String?>(null)
    val songs = keyword.flatMapLatest { keyword ->
        if (keyword.isNullOrBlank()) flowOf(null) else
            searchRepository.searchComplex(
                keyword
            ).map { it.data }
    }.stateInUi()

    val result = mapSongsFlowToUiItemsUseCase(
        songs
    ).stateInUi()

    fun updateKeyword(newKeyword: String?) {
        keyword.value = newKeyword
    }

    fun playSong(position: Int) {
        val song = songs.value?.get(position) ?: return
        (applicationScope + mainDispatcher).launch {
            playPlaylistUseCase(listOf(song))
        }
    }
}