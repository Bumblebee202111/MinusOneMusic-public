package com.github.bumblebee202111.minusonecloudmusic.ui.nowplaying

import android.Manifest
import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.ui.TimeBar
import androidx.mediarouter.app.SystemOutputSwitcherDialogController
import androidx.palette.graphics.Palette
import coil3.BitmapImage
import coil3.asDrawable
import coil3.load
import coil3.request.allowHardware
import coil3.request.error
import coil3.request.placeholder
import coil3.request.transformations
import coil3.transform.CircleCropTransformation
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.databinding.LayoutNowPlayingActionsBinding
import com.github.bumblebee202111.minusonecloudmusic.databinding.LayoutNowPlayingControlsBinding
import com.github.bumblebee202111.minusonecloudmusic.databinding.LayoutNowPlayingDiskBinding
import com.github.bumblebee202111.minusonecloudmusic.databinding.LayoutNowPlayingLyricsBinding
import com.github.bumblebee202111.minusonecloudmusic.databinding.LayoutNowPlayingProgressBinding
import com.github.bumblebee202111.minusonecloudmusic.databinding.LayoutNowPlayingToolbarBinding
import com.github.bumblebee202111.minusonecloudmusic.model.RemoteSong
import com.github.bumblebee202111.minusonecloudmusic.player.CountUtil
import com.github.bumblebee202111.minusonecloudmusic.player.RepeatShuffleModeUtil
import com.github.bumblebee202111.minusonecloudmusic.ui.common.ViewUtils
import com.github.bumblebee202111.minusonecloudmusic.ui.common.attachBadge
import com.google.android.material.badge.BadgeDrawable
import java.util.Formatter
import java.util.Locale
import android.graphics.Color as AndroidColor

@UnstableApi
@Composable
fun NowPlayingScreen(
    onNavigateBack: () -> Unit,
    onNavigateToComments: (String) -> Unit,
    onOpenPlaylist: () -> Unit,
    viewModel: NowPlayingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val lyrics by viewModel.lyrics.collectAsStateWithLifecycle()
    val player by viewModel.player.collectAsStateWithLifecycle()
    val likeState by viewModel.likeState.collectAsStateWithLifecycle()
    val commentInfo by viewModel.commentInfo.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

    val defaultBgColor = colorResource(id = R.color.default_player_background)
    var backgroundColor by remember { mutableStateOf(defaultBgColor) }

    var isLyricsMode by remember { mutableStateOf(false) }
    var songTitle by remember { mutableStateOf<String?>(null) }
    var songArtist by remember { mutableStateOf<String?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var bufferedPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var repeatMode by remember { mutableIntStateOf(Player.REPEAT_MODE_OFF) }
    var shuffleModeEnabled by remember { mutableStateOf(false) }

    val handler = remember { Handler(Looper.getMainLooper()) }
    var scrubbing by remember { mutableStateOf(false) }

    val formatBuilder = remember { java.lang.StringBuilder() }
    val formatter = remember { Formatter(formatBuilder, Locale.getDefault()) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onDownloadClick()
        }
    }
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    var isA2dpPlaying by remember { mutableStateOf(false) }
    
    DisposableEffect(Unit) {
        val audioDeviceCallback = object : AudioDeviceCallback() {
            var audioDevices: List<AudioDeviceInfo> = emptyList()
            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
                super.onAudioDevicesAdded(addedDevices)
                if (addedDevices != null) audioDevices += addedDevices
                isA2dpPlaying = audioDevices.any { it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP }
            }
            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
                super.onAudioDevicesRemoved(removedDevices)
                if (removedDevices != null) audioDevices -= removedDevices
                isA2dpPlaying = audioDevices.any { it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP }
            }
        }
        audioManager.registerAudioDeviceCallback(audioDeviceCallback, handler)
        onDispose {
            audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
            handler.removeCallbacksAndMessages(null)
        }
    }
    DisposableEffect(player) {
        val syncState = { p: Player ->
            isPlaying = p.isPlaying
            currentPosition = p.contentPosition
            bufferedPosition = p.contentBufferedPosition
            repeatMode = p.repeatMode
            shuffleModeEnabled = p.shuffleModeEnabled

            val timeline = if (p.isCommandAvailable(Player.COMMAND_GET_TIMELINE)) p.currentTimeline else Timeline.EMPTY
            if (!timeline.isEmpty) {
                val window = Timeline.Window()
                timeline.getWindow(p.currentMediaItemIndex, window)
                duration = Util.usToMs(window.durationUs)
            } else {
                val contentDuration = p.contentDuration
                duration = if (contentDuration != C.TIME_UNSET) contentDuration else 0L
            }
        }

        val listener = object : Player.Listener {
            override fun onEvents(p: Player, events: Player.Events) {
                syncState(p)
            }

            override fun onMediaMetadataChanged(mediaMetadata: androidx.media3.common.MediaMetadata) {
                songTitle = mediaMetadata.title?.toString()
                songArtist = mediaMetadata.artist?.toString()
            }
        }
        player?.addListener(listener)
        player?.let { p ->
            syncState(p)
            listener.onMediaMetadataChanged(p.mediaMetadata)
        }

        onDispose {
            player?.removeListener(listener)
        }
    }
    LaunchedEffect(isPlaying, scrubbing) {
        if (isPlaying && !scrubbing) {
            while (true) {
                player?.let { p ->
                    currentPosition = p.contentPosition
                    bufferedPosition = p.contentBufferedPosition
                }
                kotlinx.coroutines.delay(200)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AndroidViewBinding(
                factory = LayoutNowPlayingToolbarBinding::inflate,
                modifier = Modifier.fillMaxWidth(),
                update = {
                    toolbar.setNavigationOnClickListener { onNavigateBack() }
                    if (isLyricsMode) {
                        toolbar.title = songTitle.orEmpty()
                        toolbar.subtitle = songArtist.orEmpty()
                    } else {
                        toolbar.title = null
                        toolbar.subtitle = "music"
                    }
                }
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (!isLyricsMode) {
                    AndroidViewBinding(
                        factory = LayoutNowPlayingDiskBinding::inflate,
                        modifier = Modifier.fillMaxSize(),
                        update = {
                            root.setOnClickListener { isLyricsMode = true }
                            
                            val defaultArtwork = ContextCompat.getDrawable(context, R.drawable.h_7)
                            val mediaMetadata = player?.mediaMetadata
                            
                            smallAlbumCover0.load(mediaMetadata?.artworkUri ?: mediaMetadata?.artworkData) {
                                placeholder(defaultArtwork)
                                error(defaultArtwork)
                                transformations(CircleCropTransformation())
                                allowHardware(false)
                                target(
                                    onError = { error ->
                                        smallAlbumCover0.setImageDrawable(error?.asDrawable(resources))
                                        backgroundColor = defaultBgColor
                                    },
                                    onSuccess = { result ->
                                        smallAlbumCover0.setImageDrawable(result.asDrawable(resources))
                                        if (result is BitmapImage) {
                                            Palette.from(result.bitmap).generate { palette ->
                                                val dominantColor = palette?.getDominantColor(AndroidColor.BLACK) ?: AndroidColor.BLACK
                                                val hsl = FloatArray(3)
                                                ColorUtils.colorToHSL(dominantColor, hsl)
                                                hsl[2] = hsl[2].coerceIn(0.15F, 0.45F)
                                                backgroundColor = Color(AndroidColor.HSVToColor(hsl))
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    )
                } else {
                    AndroidViewBinding(
                        factory = LayoutNowPlayingLyricsBinding::inflate,
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .fillMaxHeight(),
                        update = {
                            this.lyrics.setOnClickListener { isLyricsMode = false }
                            if (lyrics != null) {
                                this.lyrics.setLyrics(lyrics)
                            }
                            this.lyrics.setPosition(currentPosition)
                            
                            lyricLikeBtn.setImageResource(if (likeState.like == true) R.drawable.h_q else R.drawable.h_o)
                            lyricLikeBtn.isEnabled = isLoggedIn == true
                            lyricLikeBtn.setOnClickListener { viewModel.onLikeClicked() }
                        }
                    )
                }
            }

            val contentModifier = Modifier.fillMaxWidth(0.9f)

            if (!isLyricsMode) {
                AndroidViewBinding(
                    factory = LayoutNowPlayingActionsBinding::inflate,
                    modifier = contentModifier,
                    update = {
                        songActionsTitle.text = songTitle
                        songActionsArtistName.text = songArtist

                        val likeBadge = BadgeDrawable.create(context).apply {
                            isVisible = true
                            setTextAppearance(R.style.TextAppearance_App_Player_Badge)
                            horizontalOffset = ViewUtils.dpToPx(context, 12).toInt()
                            verticalOffset = ViewUtils.dpToPx(context, 11).toInt()
                            this.backgroundColor = AndroidColor.TRANSPARENT
                            text = likeState.likeCountDisplayText
                        }
                        likeButton.attachBadge(likeBadge)
                        likeButton.isEnabled = isLoggedIn == true
                        likeButton.setOnClickListener { viewModel.onLikeClicked() }

                        val isLiked = likeState.like == true
                        val hasLikeCount = likeState.likeCountDisplayText != null
                        likeButton.setImageResource(
                            when {
                                isLiked && hasLikeCount -> R.drawable.hcv
                                isLiked && !hasLikeCount -> R.drawable.hcx
                                !isLiked && hasLikeCount -> R.drawable.hco
                                else -> R.drawable.hct
                            }
                        )

                        val commentBadge = BadgeDrawable.create(context).apply {
                            isVisible = true
                            setTextAppearance(R.style.TextAppearance_App_Player_Badge)
                            horizontalOffset = ViewUtils.dpToPx(context, 12).toInt()
                            verticalOffset = ViewUtils.dpToPx(context, 11).toInt()
                            this.backgroundColor = AndroidColor.TRANSPARENT
                            text = commentInfo?.commentCount?.let(CountUtil::getAbbreviatedCommentCount) ?: ""
                        }
                        commentButton.attachBadge(commentBadge)
                        commentButton.setImageResource(if (commentInfo != null) R.drawable.hbi else R.drawable.hbb)
                        commentButton.setOnClickListener { 
                            commentInfo?.threadId?.let { onNavigateToComments(it) } 
                        }
                    }
                )
            }

            AndroidViewBinding(
                factory = LayoutNowPlayingProgressBinding::inflate,
                modifier = contentModifier,
                update = {
                    if (!scrubbing) {
                        position.text = Util.getStringForTime(formatBuilder, formatter, currentPosition)
                    }
                    this.duration.text = Util.getStringForTime(formatBuilder, formatter, duration)
                    
                    timeBar.setDuration(duration)
                    timeBar.setPosition(currentPosition)
                    timeBar.setBufferedPosition(bufferedPosition)
                    
                    timeBar.addListener(object : TimeBar.OnScrubListener {
                        override fun onScrubStart(timeBar: TimeBar, pos: Long) {
                            scrubbing = true
                            position.text = Util.getStringForTime(formatBuilder, formatter, pos)
                        }
                        override fun onScrubMove(timeBar: TimeBar, pos: Long) {
                            position.text = Util.getStringForTime(formatBuilder, formatter, pos)
                        }
                        override fun onScrubStop(timeBar: TimeBar, pos: Long, canceled: Boolean) {
                            scrubbing = false
                            if (!canceled && player?.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM) == true) {
                                player?.seekTo(pos)
                            }
                        }
                    })
                }
            )

            AndroidViewBinding(
                factory = LayoutNowPlayingControlsBinding::inflate,
                modifier = contentModifier,
                update = {
                    val isPlayPauseEnabled = player?.isCommandAvailable(Player.COMMAND_PLAY_PAUSE) == true
                    playerPlayPause.isEnabled = isPlayPauseEnabled
                    @DrawableRes val drawableRes = if (isPlaying) R.drawable.ic_full_screen_player_pause else R.drawable.ic_full_screen_player_play
                    playerPlayPause.setImageResource(drawableRes)
                    playerPlayPause.setOnClickListener { player?.let { Util.handlePlayPauseButtonAction(it) } }

                    playerNext.setOnClickListener {
                        player?.let {
                            if (it.isCommandAvailable(Player.COMMAND_SEEK_TO_NEXT)) {
                                it.seekToNextMediaItem()
                                Util.handlePlayButtonAction(it)
                            }
                        }
                    }

                    playerPrev.setOnClickListener {
                        player?.let {
                            if (it.isCommandAvailable(Player.COMMAND_SEEK_TO_PREVIOUS)) {
                                it.seekToPreviousMediaItem()
                                Util.handlePlayButtonAction(it)
                            }
                        }
                    }

                    openPlaylist.setOnClickListener { onOpenPlaylist() }

                    val canShuffleRepeat = player?.isCommandAvailable(Player.COMMAND_SET_REPEAT_MODE) == true &&
                            player?.isCommandAvailable(Player.COMMAND_SET_SHUFFLE_MODE) == true
                    playerShuffleRepeat.isEnabled = canShuffleRepeat
                    
                    playerShuffleRepeat.setImageResource(
                        when (repeatMode) {
                            Player.REPEAT_MODE_ONE -> R.drawable.ic_full_screen_player_repeat_one
                            Player.REPEAT_MODE_ALL -> if (shuffleModeEnabled) R.drawable.ic_full_screen_player_repeat_all_shuffle_enabled else R.drawable.ic_full_screen_player_repeat_all_shuffle_disabled
                            else -> R.drawable.ic_full_screen_player_repeat_all_shuffle_disabled
                        }
                    )

                    playerShuffleRepeat.setOnClickListener {
                        player?.let {
                            if (canShuffleRepeat) {
                                val nextMode = RepeatShuffleModeUtil.getNextRepeatShuffleMode(it.repeatMode, it.shuffleModeEnabled)
                                it.repeatMode = nextMode.repeatMode
                                it.shuffleModeEnabled = nextMode.shuffleModeEnabled
                            }
                        }
                    }

                    deviceBtnStyle1.setImageResource(if (isA2dpPlaying) R.drawable.f5j else R.drawable.f5i)
                    deviceBtnStyle1.setOnClickListener {
                        if (!SystemOutputSwitcherDialogController.showDialog(context)) {
                            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI)
                        }
                    }

                    val downloadable = (currentSong as? RemoteSong?)?.isDownloadable == true
                    downloadButton.isVisible = downloadable
                    downloadButton.setOnClickListener {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        } else {
                            viewModel.onDownloadClick()
                        }
                    }
                    
                    ConstraintSet().apply {
                        clone(playBottomContainer)
                        if (downloadable) {
                            setHorizontalBias(R.id.deviceBtnStyle1, 1 / 6f)
                            setHorizontalBias(R.id.more_button, 5 / 6f)
                        } else {
                            setHorizontalBias(R.id.deviceBtnStyle1, 1 / 4f)
                            setHorizontalBias(R.id.more_button, 3 / 4f)
                        }
                        applyTo(playBottomContainer)
                    }
                }
            )
        }
    }
}
