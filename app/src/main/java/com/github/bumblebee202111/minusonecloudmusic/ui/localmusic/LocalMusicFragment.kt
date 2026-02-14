package com.github.bumblebee202111.minusonecloudmusic.ui.localmusic

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentLocalMusicBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PlaylistFragmentUIHelper
import com.github.bumblebee202111.minusonecloudmusic.ui.common.SimpleSongList
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocalMusicFragment : Fragment() {

    companion object {
        fun newInstance() = LocalMusicFragment()
    }

    val viewModel: LocalMusicViewModel by viewModels()
    private lateinit var binding: FragmentLocalMusicBinding
    private lateinit var uiHelper: PlaylistFragmentUIHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocalMusicBinding.inflate(inflater, container, false).apply {
            viewModel = this@LocalMusicFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uiHelper = PlaylistFragmentUIHelper(
            view = view,
            playAllAction = viewModel::playAll
        )
        
        binding.list.setContent {
            val songs by viewModel.songItems.collectAsStateWithLifecycle(initialValue = emptyList())
            SimpleSongList(
                songs = songs ?: emptyList(),
                onItemClick = viewModel::onSongItemClick
            )
        }

        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) onPermissionGranted()
            }
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_AUDIO else
            Manifest.permission.READ_EXTERNAL_STORAGE
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(permission)
        } else {
            onPermissionGranted()
        }
    }

    private fun onPermissionGranted() {
        viewModel.onPermissionGranted()
    }

}