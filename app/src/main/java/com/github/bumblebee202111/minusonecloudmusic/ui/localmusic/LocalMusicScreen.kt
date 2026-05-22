package com.github.bumblebee202111.minusonecloudmusic.ui.localmusic

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PlaylistPlayAllActions
import com.github.bumblebee202111.minusonecloudmusic.ui.common.SimpleSongList
import com.github.bumblebee202111.minusonecloudmusic.ui.common.Toolbar

@Composable
fun LocalMusicScreen(
    onNavigateBack: () -> Unit,
    viewModel: LocalMusicViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) viewModel.onPermissionGranted()
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            launcher.launch(permission)
        } else {
            viewModel.onPermissionGranted()
        }
    }

    val songs by viewModel.songItems.collectAsStateWithLifecycle(initialValue = emptyList())

    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        Toolbar(
            title = stringResource(id = R.string.title_local_music),
            onBackClick = onNavigateBack
        )

        PlaylistPlayAllActions(
            count = songs?.size ?: 0,
            onClick = { viewModel.playAll() }
        )

        SimpleSongList(
            songs = songs ?: emptyList(),
            onItemClick = viewModel::onSongItemClick,
            modifier = Modifier.weight(1f)
        )
    }
}