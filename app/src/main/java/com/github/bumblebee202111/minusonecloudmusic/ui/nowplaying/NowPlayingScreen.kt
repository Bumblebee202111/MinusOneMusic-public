package com.github.bumblebee202111.minusonecloudmusic.ui.nowplaying

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun NowPlayingScreen(
    onNavigateBack: () -> Unit,
    onNavigateToComments: (String) -> Unit,
    onOpenPlaylist: () -> Unit,
    viewModel: NowPlayingViewModel = hiltViewModel()
) {
    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val lyrics by viewModel.lyrics.collectAsStateWithLifecycle()
    val player by viewModel.player.collectAsStateWithLifecycle()
    val likeState by viewModel.likeState.collectAsStateWithLifecycle()
    val commentInfo by viewModel.commentInfo.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onDownloadClick()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            NowPlayingView(
                context = context,
                onNavigateBack = onNavigateBack,
                onNavigateToComments = onNavigateToComments,
                onOpenPlaylist = onOpenPlaylist,
                onLikeClicked = { viewModel.onLikeClicked() },
                onDownloadClick = { viewModel.onDownloadClick() },
                requestPermissionLauncher = requestPermissionLauncher
            )
        },
        update = { view ->
            view.player = player
            view.updateState(
                currentSong = currentSong,
                lyrics = lyrics,
                likeState = likeState,
                commentInfo = commentInfo,
                isLoggedIn = isLoggedIn == true
            )
        }
    )

}