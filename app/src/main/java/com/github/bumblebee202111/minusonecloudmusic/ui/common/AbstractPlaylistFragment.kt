package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.R
import javax.annotation.OverridingMethodsMustInvokeSuper

abstract class AbstractPlaylistFragment : AbstractMiniPlayerBarFragment() {

    abstract val viewModel: AbstractPlaylistViewModel<*>
    protected lateinit var playlistActions:View
    @OverridingMethodsMustInvokeSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playlistActions= view.findViewById(R.id.playlist_actions)
        playlistActions.setOnClickListener { viewModel.playAll() }
    }
    fun initSongList(songList:RecyclerView){
        val playAllListener= View.OnClickListener {
            viewModel.playAll()
        }

    }

}