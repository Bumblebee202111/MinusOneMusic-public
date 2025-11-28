package com.github.bumblebee202111.minusonecloudmusic.ui.playerhistory

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.paging.awaitNotLoading
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentDialogPlayerHistoryBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@AndroidEntryPoint
class PlayerHistoryDialogFragment : BottomSheetDialogFragment() {

    lateinit var binding: FragmentDialogPlayerHistoryBinding
    private val playerHistoryViewModel: PlayerHistoryViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDialogPlayerHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val windowManager =
            requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val windowHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds.height()
        } else {
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(displayMetrics)
            displayMetrics.heightPixels
        }
        val dialogHeight = (windowHeight * 0.72).roundToInt()

        val dialog = (requireDialog() as BottomSheetDialog)



        val bottomSheet =
            dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)!!
        val lp = (bottomSheet.layoutParams as CoordinatorLayout.LayoutParams).apply {
            height = dialogHeight
        }
        bottomSheet.layoutParams = lp

        dialog.behavior.apply {


            state = BottomSheetBehavior.STATE_EXPANDED
            peekHeight = dialogHeight
        }


        val adapter = PagedPlayerSongAdapter(onItemClick = { playlistSongItemUiModel, i ->
            playerHistoryViewModel.onItemClick(playlistSongItemUiModel.mediaId, i)
        }
        )
        val songList = binding.playlistSongs
        songList.adapter = adapter
        val linearLayoutManager = songList.layoutManager as LinearLayoutManager

        repeatWithViewLifecycle(Lifecycle.State.RESUMED) {
            launch {
                playerHistoryViewModel.songItemsPagingData.collectLatest {
                    adapter.submitData(it)

                }
            }

            launch {
                adapter.loadStateFlow.awaitNotLoading()
                playerHistoryViewModel.getCurrentSongPosition()
                    ?.let {
                        val centerOfScreen = songList.height / 2 - getItemHeight() / 2
                        linearLayoutManager.scrollToPositionWithOffset(it, centerOfScreen)
                    }
            }

        }
    }

    private fun getItemHeight(): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48F, resources.displayMetrics)
            .roundToInt()
    }

    companion object {
        const val TAG = "PlayHistoryDialogFragment"
    }
}