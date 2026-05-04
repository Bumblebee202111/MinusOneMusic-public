package com.github.bumblebee202111.minusonecloudmusic.ui.search

import androidx.appcompat.widget.SearchView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bumblebee202111.minusonecloudmusic.databinding.LayoutSearchHeaderBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.SimpleSongList

@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val songs by viewModel.result.collectAsStateWithLifecycle(initialValue = emptyList())
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        AndroidViewBinding(
            factory = LayoutSearchHeaderBinding::inflate,
            update = {
                toolbar.setNavigationOnClickListener { onNavigateBack() }
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        viewModel.updateKeyword(query)
                        keyboardController?.hide()
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        return true
                    }
                })
            }
        )

        SimpleSongList(
            songs = songs ?: emptyList(),
            onItemClick = viewModel::playSong,
            modifier = Modifier.weight(1f)
        )
    }
}