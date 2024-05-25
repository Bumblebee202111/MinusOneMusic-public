package com.github.bumblebee202111.minusonecloudmusic.ui.playlist

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentPlaylistBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.AbstractPlaylistFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.common.extractDominantColor
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import com.github.bumblebee202111.minusonecloudmusic.ui.common.setBackgroundColorAndTopCorner
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min

@AndroidEntryPoint
class PlaylistFragment : AbstractPlaylistFragment() {

    override val viewModel: PlaylistViewModel by viewModels()
    private lateinit var binding: FragmentPlaylistBinding

    companion object {
        fun newInstance() = PlaylistFragment()

        const val ARG_PLAYLIST_CREATOR_ID_UNKNOWN = -1L
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
        return binding.root
    }

    @UnstableApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { _, insets ->
            (binding.toolbar.layoutParams as ViewGroup.MarginLayoutParams).topMargin =
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            WindowInsetsCompat.CONSUMED
        }

        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange
            val fraction = min(abs(verticalOffset).toFloat() / totalScrollRange, 0.3F)

            val timeInterpolator = FastOutSlowInInterpolator()
            val interpolation = timeInterpolator.getInterpolation(fraction)
            val alpha = 0.0F + interpolation * (0.3F - 0.0F)
            binding.toolbarBackground.alpha=alpha

        }

       playlistActions.setBackgroundColorAndTopCorner(R.color.colorBackgroundAndroid,12F)

        val songAdapter = PagedPlaylistSongWithPositionAdapter(viewModel::onSongItemClick)

        binding.songList.adapter = songAdapter
        repeatWithViewLifecycle {
            launch {
                viewModel.player.collect(::setPlayer)
            }
            launch {
                viewModel.playlistDetail.collect {
                    val playlistCover = it?.coverImgUrl?:return@collect
                    Log.d("fuvk", playlistCover)
                    Glide.with(binding.playlistCover).load(playlistCover)
                        .placeholder(R.drawable.h_1)
                        .extractDominantColor(binding.gradientBg, Color.BLACK,0.45F,0.75F)
                        .optionalCenterCrop().into(binding.playlistCover)

                }


            }
            launch {
                viewModel.playlistSongs.collectLatest {
                    songAdapter.submitData(it)

                }
            }
        }

    }


}