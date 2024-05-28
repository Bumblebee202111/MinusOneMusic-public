package com.github.bumblebee202111.minusonecloudmusic.ui.recentplay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentMyRecentPlayMusicBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.AbstractPlaylistFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.common.AbstractPlaylistViewModel
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import com.github.bumblebee202111.minusonecloudmusic.ui.common.songadapters.SimpleSongAdapter
import kotlinx.coroutines.launch

class MyRecentPlayMusicFragment : AbstractPlaylistFragment() {

    private val myRecentPlayViewModel:MyRecentPlayViewModel by viewModels( { requireParentFragment() })
    override val viewModel: AbstractPlaylistViewModel<*>
        get() = myRecentPlayViewModel
    private lateinit var binding: FragmentMyRecentPlayMusicBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=FragmentMyRecentPlayMusicBinding.inflate(inflater,container,false).apply {
            lifecycleOwner=viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = SimpleSongAdapter {
            viewModel.onSongItemClick(it)
        }
        binding.list.adapter=adapter

        repeatWithViewLifecycle {
            launch {
                myRecentPlayViewModel.recentPlaySongUiList.collect{
                    adapter.submitList(it)
                }
            }
            launch {
                myRecentPlayViewModel.player.collect{
                    binding.miniPlayerBar.player=it
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