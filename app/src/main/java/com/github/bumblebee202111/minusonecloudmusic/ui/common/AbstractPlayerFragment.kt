package com.github.bumblebee202111.minusonecloudmusic.ui.common

import androidx.fragment.app.Fragment
import com.github.bumblebee202111.minusonecloudmusic.ui.playerhistory.PlayerHistoryDialogFragment

abstract class AbstractPlayerFragment:Fragment() {
    fun openPlayerPlaylistDialog(){
        PlayerHistoryDialogFragment().show(parentFragmentManager, PlayerHistoryDialogFragment.TAG)
    }
}