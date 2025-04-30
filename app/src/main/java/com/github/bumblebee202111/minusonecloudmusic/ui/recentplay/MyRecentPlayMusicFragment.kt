package com.github.bumblebee202111.minusonecloudmusic.ui.recentplay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentMyRecentPlayMusicBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PlaylistFragmentUIHelper
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import com.github.bumblebee202111.minusonecloudmusic.ui.common.songadapters.SimpleSongAdapter
import kotlinx.coroutines.launch

class MyRecentPlayMusicFragment : Fragment() {

    private val viewModel: MyRecentPlayViewModel by viewModels({ requireParentFragment() })
    private lateinit var binding: FragmentMyRecentPlayMusicBinding
    private lateinit var uiHelper: PlaylistFragmentUIHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyRecentPlayMusicBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uiHelper = PlaylistFragmentUIHelper(
            view = view,
            playAllAction = viewModel::playAll
        )
        val adapter = SimpleSongAdapter {
            viewModel.onSongItemClick(it)
        }
        binding.list.adapter = adapter

        repeatWithViewLifecycle {
            launch {
                viewModel.recentPlaySongUiList.collect {
                    adapter.submitList(it)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            MyRecentPlayMusicFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}