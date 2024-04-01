package com.github.bumblebee202111.minusonecloudmusic.ui.playlist

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.findNavController
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.data.model.Song
import com.github.bumblebee202111.minusonecloudmusic.data.model.asMediaItem
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentPlaylistBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.MiniPlayerBarView
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlaylistFragment : Fragment() {

    private val viewModel: PlaylistViewModel by viewModels()
    private lateinit var binding: FragmentPlaylistBinding
    private lateinit var playerView: MiniPlayerBarView
    companion object {
        fun newInstance() = PlaylistFragment()
    }

    @UnstableApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaylistBinding.inflate(layoutInflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@PlaylistFragment.viewModel
        }
        playerView = binding.playerView
        return binding.root
    }

    @UnstableApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        playerView.setOnClickListener {
            findNavController().navigate(R.id.nav_now_playing)
        }

        repeatWithViewLifecycle {
            launch {
                viewModel.player.collect{
                    binding.playerView.player = it
                }
            }

        }

        val songAdapter = SongAdapter { song ->
            if (song.available) {
                val player = viewModel.player.value ?: return@SongAdapter
                with(player) {
                    val songs =
                        viewModel.playlistDetail.value?.songs ?: emptyList()

                    val mediaItems = songs.map(Song::asMediaItem)

                    val startPositionMs =
                        if (currentMediaItem?.mediaId == song.id.toString()) currentPosition
                        else C.TIME_UNSET
                    setMediaItems(
                        mediaItems,
                        songs.indexOfFirst { it.id == song.id },
                        startPositionMs
                    )
                    prepare()
                    play()

                }

            }

        }
        binding.songList.adapter = songAdapter
        repeatWithViewLifecycle {
            launch {
                viewModel.playlistDetail.collect {
                    songAdapter.submitList(it?.songs)
                }

            }
        }
    }


    @UnstableApi
    override fun onStop() {
        super.onStop()
        playerView.player = null
    }


}