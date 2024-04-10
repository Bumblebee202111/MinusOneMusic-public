package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.os.Bundle
import android.view.View
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.findNavController
import com.github.bumblebee202111.minusonecloudmusic.R
import javax.annotation.OverridingMethodsMustInvokeSuper

@OptIn(UnstableApi::class)
abstract class AbstractMiniPlayerBarFragment: AbstractPlayerFragment() {

    lateinit var miniPlayerBar:MiniPlayerBarView

    @OverridingMethodsMustInvokeSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        miniPlayerBar=view.findViewById<MiniPlayerBarView>(R.id.mini_player_bar).apply {
            setOnClickListener {
                findNavController().navigate(R.id.nav_now_playing)
            }
            setPlaylistButtonListener{
                openPlayerPlaylistDialog()
            }
        }

    }

    @OverridingMethodsMustInvokeSuper
    override fun onStop() {
        super.onStop()
        miniPlayerBar.player=null
    }

    fun setPlayer(player: Player?){
        miniPlayerBar.player=player
    }

}