package com.github.bumblebee202111.minusonecloudmusic.ui.common

import androidx.fragment.app.FragmentManager
import com.github.bumblebee202111.minusonecloudmusic.ui.playerhistory.PlayerHistoryDialogFragment

class PlaylistDialogController(
    private val fragmentManager: FragmentManager
) {
    fun showPlayerPlaylistDialog() {
        if (fragmentManager.findFragmentByTag(PlayerHistoryDialogFragment.TAG) == null) {
            PlayerHistoryDialogFragment().show(fragmentManager, PlayerHistoryDialogFragment.TAG)
        }
    }
}