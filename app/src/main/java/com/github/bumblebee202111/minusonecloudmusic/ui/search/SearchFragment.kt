package com.github.bumblebee202111.minusonecloudmusic.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bumblebee202111.minusonecloudmusic.databinding.LayoutSearchHeaderBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.SimpleSongList
import com.github.bumblebee202111.minusonecloudmusic.ui.common.hideSoftInput
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.NavigationManager
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private val viewModel: SearchViewModel by viewModels()

    @Inject
    lateinit var navigationManager: NavigationManager
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DolphinTheme {
                    val songs by viewModel.result.collectAsStateWithLifecycle(initialValue = emptyList())

                    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
                        AndroidViewBinding(
                            factory = LayoutSearchHeaderBinding::inflate,
                            update = {
                                toolbar.setNavigationOnClickListener { navigationManager.goBack() }
                                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                                    override fun onQueryTextSubmit(query: String?): Boolean {
                                        viewModel.updateKeyword(query)
                                        hideSoftInput()
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
            }
        }
    }
}