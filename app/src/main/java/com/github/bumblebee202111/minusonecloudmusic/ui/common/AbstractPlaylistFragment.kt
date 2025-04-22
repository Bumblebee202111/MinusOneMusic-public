package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.R
import kotlinx.coroutines.launch
import javax.annotation.OverridingMethodsMustInvokeSuper

abstract class AbstractPlaylistFragment : Fragment() {

    abstract val viewModel: AbstractPlaylistViewModel<*>
    private lateinit var playlistDialogController: PlaylistDialogController
    protected lateinit var miniPlayerBarController: MiniPlayerBarController
    protected lateinit var playlistActions:View
    @OverridingMethodsMustInvokeSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playlistDialogController = PlaylistDialogController(parentFragmentManager)
        miniPlayerBarController = MiniPlayerBarController(
            view = view,
            navController = findNavController(),
            playlistDialogController = playlistDialogController
        )

        playlistActions= view.findViewById(R.id.playlist_actions)
        playlistActions.setOnClickListener { viewModel.playAll() }


    }
    fun initSongList(songList:RecyclerView){
        val playAllListener= View.OnClickListener {
            viewModel.playAll()
        }
    }

    @OverridingMethodsMustInvokeSuper
    override fun onStop() {
        miniPlayerBarController.onStop()
        super.onStop()
    }
}