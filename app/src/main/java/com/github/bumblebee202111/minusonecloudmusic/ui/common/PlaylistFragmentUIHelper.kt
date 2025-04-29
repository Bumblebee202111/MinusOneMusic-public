package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.github.bumblebee202111.minusonecloudmusic.R

class PlaylistFragmentUIHelper(
    private val fragment: Fragment,
    private val view: View,
    private val navController: NavController,
    private val playAllAction: () -> Unit
) {
    val playlistDialogController: PlaylistDialogController = PlaylistDialogController(fragment.parentFragmentManager)
    val miniPlayerBarController: MiniPlayerBarController = MiniPlayerBarController(
        view = view,
        navController = navController,
        playlistDialogController = playlistDialogController
    )

    init {
        val playlistActions = view.findViewById<View>(R.id.playlist_actions)
        playlistActions.setOnClickListener { playAllAction() }
    }
    fun onStop() {
        miniPlayerBarController.onStop()
    }
}