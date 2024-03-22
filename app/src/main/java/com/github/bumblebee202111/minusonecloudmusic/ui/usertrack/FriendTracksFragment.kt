package com.github.bumblebee202111.minusonecloudmusic.ui.usertrack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.bumblebee202111.minusonecloudmusic.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendTracksFragment : Fragment() {

    companion object {
        fun newInstance() = FriendTracksFragment()
    }

    private lateinit var viewModel: FriendTracksViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_friend_tracks, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FriendTracksViewModel::class.java)
    }

}