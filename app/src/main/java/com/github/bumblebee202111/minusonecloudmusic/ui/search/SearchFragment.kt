package com.github.bumblebee202111.minusonecloudmusic.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.compose.runtime.getValue
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentSearchBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.SimpleSongList
import com.github.bumblebee202111.minusonecloudmusic.ui.common.hideSoftInput
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.NavigationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment() {

    lateinit var binding: FragmentSearchBinding
    private val viewModel: SearchViewModel by viewModels()

    @Inject
    lateinit var navigationManager: NavigationManager
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            navigationManager.goBack()
        }

        binding.searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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

        binding.songList.setContent {
            val songs by viewModel.result.collectAsStateWithLifecycle(initialValue = emptyList())
            SimpleSongList(
                songs = songs ?: emptyList(),
                onItemClick = viewModel::playSong
            )
        }
    }

}