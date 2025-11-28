package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.view.View
import com.github.bumblebee202111.minusonecloudmusic.R

class PlaylistFragmentUIHelper(
    private val view: View,
    private val playAllAction: () -> Unit
) {

    init {
        val playlistActions = view.findViewById<View>(R.id.playlist_actions)
        playlistActions.setOnClickListener { playAllAction() }
    }
}