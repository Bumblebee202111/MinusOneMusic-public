package com.github.bumblebee202111.minusonecloudmusic.ui.playlist

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.media3.common.util.UnstableApi
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.load
import coil3.request.error
import coil3.request.placeholder
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentPlaylistBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PagedSongWithPositionList
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PlaylistFragmentUIHelper
import com.github.bumblebee202111.minusonecloudmusic.ui.common.applyDominantColor
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import com.github.bumblebee202111.minusonecloudmusic.ui.common.setBackgroundColorAndTopCorner
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.NavigationManager
import com.google.android.material.appbar.MaterialToolbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.min

@AndroidEntryPoint
class PlaylistFragment : Fragment() {

    val viewModel: PlaylistViewModel by viewModels()
    private lateinit var binding: FragmentPlaylistBinding
    private lateinit var uiHelper: PlaylistFragmentUIHelper
    private lateinit var playlistActions: View
    private lateinit var toolbar: MaterialToolbar
    private lateinit var toolbarBackground: View

    @Inject
    lateinit var navigationManager: NavigationManager
    companion object {
        fun newInstance() = PlaylistFragment()

        const val ARG_VALUE_PLAYLIST_CREATOR_ID_UNKNOWN = -1L
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
        toolbar = binding.toolbar
        toolbarBackground = binding.toolbarBackground

        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { _, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            val toolbarSize = resources.getDimensionPixelSize(R.dimen.toolbar_size)
            (toolbar.layoutParams as ViewGroup.MarginLayoutParams).topMargin =
                topInset
            (binding.playlistDetail.layoutParams as ViewGroup.MarginLayoutParams).topMargin =
                topInset + toolbarSize

            toolbarBackground.run {
                layoutParams.height =
                    toolbarSize + topInset
                isVisible = true
                requestLayout()
            }

            WindowInsetsCompat.CONSUMED
        }

        toolbar.setNavigationOnClickListener {
            navigationManager.goBack()
        }

        playlistActions= view.findViewById(R.id.playlist_actions)

        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange
            val fraction = min(abs(verticalOffset).toFloat() / totalScrollRange, 0.3F)

            val timeInterpolator = FastOutSlowInInterpolator()
            val interpolation = timeInterpolator.getInterpolation(fraction)
            val alpha = 0.0F + interpolation * (0.3F - 0.0F)
            toolbarBackground.alpha = alpha

        }

       playlistActions.setBackgroundColorAndTopCorner(R.color.colorBackgroundAndroid, 12F)
        uiHelper = PlaylistFragmentUIHelper(
            view = view,
            playAllAction = viewModel::playAll
        )
        
        binding.songList.setContent {
            val songs = viewModel.playlistSongs.collectAsLazyPagingItems()
            PagedSongWithPositionList(
                songs = songs,
                onItemClick = viewModel::onSongItemClick
            )
        }

        repeatWithViewLifecycle {
            launch {
                viewModel.playlistDetail.collect {
                    val playlistCover = it?.coverImgUrl ?: return@collect
                    Log.d("fuvk", playlistCover)
                    binding.playlistCover.load(playlistCover) {
                        placeholder(R.drawable.h_1)
                        error(R.drawable.h_1)
                        listener(
                            onSuccess = { _, result ->
                                applyDominantColor(
                                    result = result,
                                    targetView = binding.gradientBg,
                                    defaultColor = Color.BLACK,
                                    minL = 0.45F,
                                    maxL = 0.75F
                                )
                            },
                            onError = { _, _ ->
                                binding.gradientBg.setBackgroundColor(Color.BLACK)
                            }
                        )

                    }


                }

            }
        }
    }

}