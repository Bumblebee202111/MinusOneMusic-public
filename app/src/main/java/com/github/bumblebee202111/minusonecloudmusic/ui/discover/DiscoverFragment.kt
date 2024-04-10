package com.github.bumblebee202111.minusonecloudmusic.ui.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentDiscoverBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.mainNavController
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import com.github.bumblebee202111.minusonecloudmusic.ui.main.MainFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.playlist.PlaylistSongAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
@AndroidEntryPoint
class DiscoverFragment : Fragment() {

    companion object {
        fun newInstance() = DiscoverFragment()
    }

    private lateinit var binding: FragmentDiscoverBinding

    private lateinit var mainFragment: MainFragment

    val viewModel: DiscoverViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDiscoverBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categories = requireActivity().resources.getStringArray(R.array.wow_types).toList()
        val playListSquareAdapter = PlayListSquareAdapter(categories) { position ->
            when (position) {
                0 -> {
                    mainNavController.navigate(R.id.nav_dailyrecommend)
                }

                3 -> {
                    mainNavController.navigate(R.id.nav_toplist)
                }

                else -> throw IndexOutOfBoundsException()
            }
        }
        binding.wowDashboard.adapter = playListSquareAdapter

        val playlistSongAdapter = PlaylistSongAdapter{ song->
            viewModel.onSongItemClick(song.id)
        }
        binding.hotSongsChartList.adapter = playlistSongAdapter

        repeatWithViewLifecycle {
            launch {
                viewModel.songUiList.collect {
                    playlistSongAdapter.submitList(it)
                }
            }
        }
    }
}
