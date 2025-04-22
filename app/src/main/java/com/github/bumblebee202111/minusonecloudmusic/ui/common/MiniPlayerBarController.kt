package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.view.View
import androidx.media3.common.Player
import androidx.navigation.NavController
import com.github.bumblebee202111.minusonecloudmusic.R

class MiniPlayerBarController(
    view: View,
    private val navController: NavController,
    private val playlistDialogController: PlaylistDialogController
) {
    private val miniPlayerBar = view.findViewById<MiniPlayerBarView>(R.id.mini_player_bar)

    init {
        miniPlayerBar.setOnClickListener {
            navController.navigate(R.id.nav_now_playing)
        }
        miniPlayerBar.setPlaylistButtonListener {
           playlistDialogController.showPlayerPlaylistDialog()
        }
    }

    fun setPlayer(player: Player?) {
        miniPlayerBar.player = player
    }

    fun onStop() {
        miniPlayerBar.player = null
    }
}