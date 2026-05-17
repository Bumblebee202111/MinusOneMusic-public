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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.graphics.ColorUtils
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.ui.TimeBar
import androidx.mediarouter.app.SystemOutputSwitcherDialogController
import androidx.palette.graphics.Palette
import coil3.BitmapImage
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.databinding.LayoutNowPlayingActionsBinding
import com.github.bumblebee202111.minusonecloudmusic.databinding.LayoutNowPlayingLyricsBinding
import com.github.bumblebee202111.minusonecloudmusic.databinding.LayoutNowPlayingProgressBinding
import com.github.bumblebee202111.minusonecloudmusic.databinding.LayoutNowPlayingToolbarBinding
import com.github.bumblebee202111.minusonecloudmusic.model.CommentInfo
import com.github.bumblebee202111.minusonecloudmusic.model.LyricsEntry
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

            val timeline =
                if (p.isCommandAvailable(Player.COMMAND_GET_TIMELINE)) p.currentTimeline else Timeline.EMPTY
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

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                songTitle = mediaMetadata.title?.toString()
                songArtist = mediaMetadata.artist?.toString()
            }
        }
        player?.addListener(listener)
        player?.let { p ->
            syncState(p)
            listener.onMediaMetadataChanged(p.mediaMetadata)
        }

        onDispose { player?.removeListener(listener) }
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
            NowPlayingToolbar(
                isLyricsMode = isLyricsMode,
                songTitle = songTitle,
                songArtist = songArtist,
                onNavigateBack = onNavigateBack
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (!isLyricsMode) {
                    NowPlayingDisk(
                        mediaMetadata = player?.mediaMetadata,
                        defaultBgColor = defaultBgColor,
                        onBackgroundColorChange = { backgroundColor = it },
                        onClick = { isLyricsMode = true }
                    )
                } else {
                    NowPlayingLyrics(
                        lyrics = lyrics,
                        currentPosition = currentPosition,
                        likeState = likeState,
                        isLoggedIn = isLoggedIn,
                        onLikeClicked = { viewModel.onLikeClicked() },
                        onClick = { isLyricsMode = false }
                    )
                }
            }

            val contentModifier = Modifier.fillMaxWidth(0.9f)

            if (!isLyricsMode) {
                NowPlayingActions(
                    modifier = contentModifier,
                    songTitle = songTitle,
                    songArtist = songArtist,
                    likeState = likeState,
                    commentInfo = commentInfo,
                    isLoggedIn = isLoggedIn,
                    onLikeClicked = { viewModel.onLikeClicked() },
                    onNavigateToComments = onNavigateToComments
                )
            }

            NowPlayingProgress(
                modifier = contentModifier,
                currentPosition = currentPosition,
                bufferedPosition = bufferedPosition,
                duration = duration,
                scrubbing = scrubbing,
                player = player,
                onScrubbingChange = { isScrubbing, position ->
                    scrubbing = isScrubbing
                    if (position != null) {
                        currentPosition = position
                    }
                }
            )

            NowPlayingControls(
                modifier = contentModifier,
                player = player,
                isPlaying = isPlaying,
                repeatMode = repeatMode,
                shuffleModeEnabled = shuffleModeEnabled,
                isA2dpPlaying = isA2dpPlaying,
                downloadable = (currentSong as? RemoteSong?)?.isDownloadable == true,
                audioManager = audioManager,
                onOpenPlaylist = onOpenPlaylist,
                onDownloadClick = { viewModel.onDownloadClick() }
            )
        }
    }
}

@Composable
private fun NowPlayingToolbar(
    isLyricsMode: Boolean,
    songTitle: String?,
    songArtist: String?,
    onNavigateBack: () -> Unit
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
}

@Composable
private fun NowPlayingDisk(
    mediaMetadata: MediaMetadata?,
    defaultBgColor: Color,
    onBackgroundColorChange: (Color) -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(1f / 1.2f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.hal),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .align(Alignment.BottomCenter),
                contentScale = ContentScale.Fit
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .align(BiasAlignment(0f, 2f / 3f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(mediaMetadata?.artworkUri ?: mediaMetadata?.artworkData)
                        .allowHardware(false)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxHeight(0.71f)
                        .aspectRatio(1f)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.h_7),
                    error = painterResource(id = R.drawable.h_7),
                    onSuccess = { state ->
                        val image = state.result.image
                        if (image is BitmapImage) {
                            Palette.from(image.bitmap).generate { palette ->
                                val dominantColor =
                                    palette?.getDominantColor(AndroidColor.BLACK)
                                        ?: AndroidColor.BLACK
                                val hsl = FloatArray(3)
                                ColorUtils.colorToHSL(dominantColor, hsl)
                                hsl[2] = hsl[2].coerceIn(0.15F, 0.45F)
                                onBackgroundColorChange(Color(AndroidColor.HSVToColor(hsl)))
                            }
                        }
                    },
                    onError = {
                        onBackgroundColorChange(defaultBgColor)
                    }
                )
                Image(
                    painter = painterResource(id = R.drawable.hah),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            Image(
                painter = painterResource(id = R.drawable.her),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f)
                    .align(Alignment.TopCenter),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun NowPlayingLyrics(
    lyrics: List<LyricsEntry>?,
    currentPosition: Long,
    likeState: LikeState,
    isLoggedIn: Boolean?,
    onLikeClicked: () -> Unit,
    onClick: () -> Unit
) {
    AndroidViewBinding(
        factory = LayoutNowPlayingLyricsBinding::inflate,
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .fillMaxHeight(),
        update = {
            this.lyrics.setOnClickListener { onClick() }
            if (lyrics != null) {
                this.lyrics.setLyrics(lyrics)
            }
            this.lyrics.setPosition(currentPosition)

            lyricLikeBtn.setImageResource(if (likeState.like == true) R.drawable.h_q else R.drawable.h_o)
            lyricLikeBtn.isEnabled = isLoggedIn == true
            lyricLikeBtn.setOnClickListener { onLikeClicked() }
        }
    )
}

@Composable
private fun NowPlayingActions(
    modifier: Modifier = Modifier,
    songTitle: String?,
    songArtist: String?,
    likeState: LikeState,
    commentInfo: CommentInfo?,
    isLoggedIn: Boolean?,
    onLikeClicked: () -> Unit,
    onNavigateToComments: (String) -> Unit
) {
    val context = LocalContext.current

    AndroidViewBinding(
        factory = LayoutNowPlayingActionsBinding::inflate,
        modifier = modifier,
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
            likeButton.setOnClickListener { onLikeClicked() }

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

@UnstableApi
@Composable
private fun NowPlayingProgress(
    modifier: Modifier = Modifier,
    currentPosition: Long,
    bufferedPosition: Long,
    duration: Long,
    scrubbing: Boolean,
    player: Player?,
    onScrubbingChange: (isScrubbing: Boolean, position: Long?) -> Unit
) {
    val formatBuilder = remember { java.lang.StringBuilder() }
    val formatter = remember { Formatter(formatBuilder, Locale.getDefault()) }

    AndroidViewBinding(
        factory = LayoutNowPlayingProgressBinding::inflate,
        modifier = modifier,
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
                    onScrubbingChange(true, pos)
                    position.text = Util.getStringForTime(formatBuilder, formatter, pos)
                }

                override fun onScrubMove(timeBar: TimeBar, pos: Long) {
                    onScrubbingChange(true, pos)
                    position.text = Util.getStringForTime(formatBuilder, formatter, pos)
                }

                override fun onScrubStop(timeBar: TimeBar, pos: Long, canceled: Boolean) {
                    onScrubbingChange(false, null)
                    if (!canceled && player?.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM) == true) {
                        player.seekTo(pos)
                    }
                }
            })
        }
    )
}

@Composable
private fun NowPlayingControls(
    modifier: Modifier = Modifier,
    player: Player?,
    isPlaying: Boolean,
    repeatMode: Int,
    shuffleModeEnabled: Boolean,
    isA2dpPlaying: Boolean,
    downloadable: Boolean,
    audioManager: AudioManager,
    onOpenPlaylist: () -> Unit,
    onDownloadClick: () -> Unit
) {
    val context = LocalContext.current
    val isPlayPauseEnabled = player?.isCommandAvailable(Player.COMMAND_PLAY_PAUSE) == true
    val canShuffleRepeat =
        player?.isCommandAvailable(Player.COMMAND_SET_REPEAT_MODE) == true && player.isCommandAvailable(
            Player.COMMAND_SET_SHUFFLE_MODE
        )

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onDownloadClick()
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(
                    when (repeatMode) {
                        Player.REPEAT_MODE_ONE -> R.drawable.ic_full_screen_player_repeat_one
                        Player.REPEAT_MODE_ALL -> if (shuffleModeEnabled) R.drawable.ic_full_screen_player_repeat_all_shuffle_enabled else R.drawable.ic_full_screen_player_repeat_all_shuffle_disabled
                        else -> R.drawable.ic_full_screen_player_repeat_all_shuffle_disabled
                    }
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = canShuffleRepeat
                    ) {
                        player?.let {
                            val nextMode = RepeatShuffleModeUtil.getNextRepeatShuffleMode(
                                it.repeatMode,
                                it.shuffleModeEnabled
                            )
                            it.repeatMode = nextMode.repeatMode
                            it.shuffleModeEnabled = nextMode.shuffleModeEnabled
                        }
                    },
                contentScale = ContentScale.Fit
            )

            Image(
                painter = painterResource(id = R.drawable.ic_full_screen_player_prev),
                contentDescription = "Previous",
                modifier = Modifier
                    .size(50.dp)
                    .alpha(0.7f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        player?.let {
                            if (it.isCommandAvailable(Player.COMMAND_SEEK_TO_PREVIOUS)) {
                                it.seekToPreviousMediaItem()
                                Util.handlePlayButtonAction(it)
                            }
                        }
                    }
                    .padding(7.dp),
                contentScale = ContentScale.Fit
            )

            Image(
                painter = painterResource(
                    id = if (isPlaying) R.drawable.ic_full_screen_player_pause else R.drawable.ic_full_screen_player_play
                ),
                contentDescription = "Play/Pause",
                modifier = Modifier
                    .size(64.dp)
                    .alpha(0.7f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = isPlayPauseEnabled
                    ) {
                        player?.let { Util.handlePlayPauseButtonAction(it) }
                    }
                    .padding(14.dp),
                contentScale = ContentScale.Fit
            )

            Image(
                painter = painterResource(id = R.drawable.ic_full_screen_player_next),
                contentDescription = "Next",
                modifier = Modifier
                    .size(50.dp)
                    .alpha(0.7f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        player?.let {
                            if (it.isCommandAvailable(Player.COMMAND_SEEK_TO_NEXT)) {
                                it.seekToNextMediaItem()
                                Util.handlePlayButtonAction(it)
                            }
                        }
                    }
                    .padding(7.dp),
                contentScale = ContentScale.Fit
            )

            Image(
                painter = painterResource(id = R.drawable.ic_full_screen_playlist),
                contentDescription = "Playlist",
                modifier = Modifier
                    .size(40.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onOpenPlaylist() },
                contentScale = ContentScale.Fit
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = if (isA2dpPlaying) R.drawable.f5j else R.drawable.f5i),
                contentDescription = "Device",
                colorFilter = ColorFilter.tint(Color(0xFFF9F9F9)),
                modifier = Modifier
                    .size(34.dp)
                    .alpha(0.4f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (!SystemOutputSwitcherDialogController.showDialog(context)) {
                            audioManager.adjustStreamVolume(
                                AudioManager.STREAM_MUSIC,
                                AudioManager.ADJUST_SAME,
                                AudioManager.FLAG_SHOW_UI
                            )
                        }
                    }
                    .padding(7.dp),
                contentScale = ContentScale.Fit
            )

            if (downloadable) {
                Image(
                    painter = painterResource(id = R.drawable.ic_full_screen_player_download),
                    contentDescription = "Download",
                    modifier = Modifier
                        .size(34.dp)
                        .alpha(0.4f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            } else {
                                onDownloadClick()
                            }
                        }
                        .padding(7.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Image(
                painter = painterResource(id = R.drawable.ic_full_screen_player_more),
                contentDescription = "More",
                modifier = Modifier
                    .size(34.dp)
                    .alpha(0.4f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {  }
                    .padding(7.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}