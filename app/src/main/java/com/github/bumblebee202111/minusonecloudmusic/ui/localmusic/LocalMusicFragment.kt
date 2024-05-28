package com.github.bumblebee202111.minusonecloudmusic.ui.localmusic

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentLocalMusicBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.AbstractPlaylistFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import com.github.bumblebee202111.minusonecloudmusic.ui.common.songadapters.SimpleSongAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LocalMusicFragment : AbstractPlaylistFragment() {

    companion object {
        fun newInstance() = LocalMusicFragment()
    }

    override val viewModel: LocalMusicViewModel by viewModels()
    private lateinit var binding: FragmentLocalMusicBinding
    private lateinit var adapter: SimpleSongAdapter
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

        adapter = SimpleSongAdapter(viewModel::onSongItemClick)
        binding.list.adapter = adapter
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
        repeatWithViewLifecycle {
            launch {
                viewModel.songItems.collect(adapter::submitList)
            }
            launch {
                viewModel.player.collect(::setPlayer)
            }
        }
    }

    private fun onPermissionGranted() {
        viewModel.onPermissionGranted()
    }

}