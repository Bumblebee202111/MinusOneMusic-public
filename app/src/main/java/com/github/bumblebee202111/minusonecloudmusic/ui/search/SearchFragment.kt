package com.github.bumblebee202111.minusonecloudmusic.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentSearchBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.AbstractMiniPlayerBarFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.common.hideSoftInput
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import com.github.bumblebee202111.minusonecloudmusic.ui.common.songadapters.SimpleSongAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : AbstractMiniPlayerBarFragment() {

    lateinit var binding: FragmentSearchBinding

    private val viewModel: SearchViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

        val adapter = SimpleSongAdapter(viewModel::playSong)
        binding.songList.adapter = adapter

        repeatWithViewLifecycle {
            launch {
                viewModel.result.collect(adapter::submitList)
            }
            launch {
                viewModel.player.collect(::setPlayer)
            }
        }
    }
}