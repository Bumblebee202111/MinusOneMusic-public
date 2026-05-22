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
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.ColorUtils
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.mediarouter.app.SystemOutputSwitcherDialogController
import androidx.palette.graphics.Palette
import coil3.BitmapImage
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.model.CommentInfo
import com.github.bumblebee202111.minusonecloudmusic.model.LyricsEntry
import com.github.bumblebee202111.minusonecloudmusic.model.RemoteSong
import com.github.bumblebee202111.minusonecloudmusic.player.CountUtil
import com.github.bumblebee202111.minusonecloudmusic.player.RepeatShuffleModeUtil
import com.github.bumblebee202111.minusonecloudmusic.ui.common.LyricsView
import com.github.bumblebee202111.minusonecloudmusic.ui.common.Toolbar
import kotlinx.coroutines.delay
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

    val defaultBgColor = Color(0xFF262626)
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
                delay(200)
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
    Toolbar(
        onBackClick = onNavigateBack,
        iconRes = R.drawable.yg,
        iconTint = Color(0x99FFFFFF),
        centerContent = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isLyricsMode && !songTitle.isNullOrEmpty()) {
                    Text(
                        text = songTitle,
                        color = Color(0xEEFFFFFF),
                        fontSize = 13.sp,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                }
                val subtitle = if (isLyricsMode) songArtist.orEmpty() else "music"
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        color = Color(0xBBFFFFFF),
                        fontSize = 12.sp,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                }
            }
        },
        trailingContent = {
            IconButton(
                onClick = {  },
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(48.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.hxo),
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
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
    val x8 = dimensionResource(id = R.dimen.x8)

    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .fillMaxHeight()
            .padding(bottom = 12.dp)
    ) {
        AndroidView(
            factory = { context ->
                LyricsView(context).apply {
                    setOnClickListener { onClick() }
                }
            },
            update = { view ->
                if (lyrics != null) {
                    view.setLyrics(lyrics)
                }
                view.setPosition(currentPosition)
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 16.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(x8),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = if (likeState.like == true) R.drawable.h_q else R.drawable.h_o),
                    contentDescription = "Like",
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(x8)
                        .alpha(0.6f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = isLoggedIn == true
                        ) { onLikeClicked() },
                    contentScale = ContentScale.Inside
                )
                Image(
                    painter = painterResource(id = R.drawable.h_n),
                    contentDescription = "MLog",
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(x8)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {  }
                        .padding(vertical = 1.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Image(
                painter = painterResource(id = R.drawable.g71),
                contentDescription = "Options",
                modifier = Modifier
                    .size(x8)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {  }
                    .padding(4.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
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
    val density = LocalDensity.current
    val titleTextSize = with(density) { 17.dp.toSp() }
    val artistTextSize = with(density) { 14.dp.toSp() }
    val badgeTextSize = with(density) { 9.dp.toSp() }

    Row(
        modifier = modifier.height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = songTitle.orEmpty(),
                color = Color(0xFFEEF6EC).copy(alpha = 0.7f),
                fontSize = titleTextSize,
                maxLines = 1,
                modifier = Modifier.basicMarquee()
            )

            Text(
                text = songArtist.orEmpty(),
                color = Color(0xFFEEF6EC).copy(alpha = 0.4f),
                fontSize = artistTextSize,
                maxLines = 1,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .basicMarquee()
            )
        }
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            val isLiked = likeState.like == true
            val hasLikeCount = likeState.likeCountDisplayText != null
            val likeIconRes = when {
                isLiked && hasLikeCount -> R.drawable.hcv
                isLiked && !hasLikeCount -> R.drawable.hcx
                !isLiked && hasLikeCount -> R.drawable.hco
                else -> R.drawable.hct
            }

            BadgedBox(
                badge = {
                    if (hasLikeCount) {
                        Text(
                            text = likeState.likeCountDisplayText,
                            color = Color(0xB3FFFFFF),
                            fontSize = badgeTextSize,
                            fontFamily = FontFamily(Font(R.font.a)),
                            maxLines = 1,
                            modifier = Modifier.offset(x = (-1).dp, y = 10.dp)
                        )
                    }
                }
            ) {
                Image(
                    painter = painterResource(id = likeIconRes),
                    contentDescription = "Like",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = isLoggedIn == true
                        ) { onLikeClicked() }
                        .padding(5.dp),
                    contentScale = ContentScale.Inside
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            BadgedBox(
                badge = {
                    val count =
                        commentInfo?.commentCount?.let(CountUtil::getAbbreviatedCommentCount)
                    if (!count.isNullOrEmpty()) {
                        Text(
                            text = count,
                            color = Color(0xB3FFFFFF),
                            fontSize = badgeTextSize,
                            fontFamily = FontFamily(Font(R.font.a)),
                            maxLines = 1,
                            modifier = Modifier.offset(x = (-1).dp, y = 10.dp)
                        )
                    }
                }
            ) {
                Image(
                    painter = painterResource(id = if (commentInfo != null) R.drawable.hbi else R.drawable.hbb),
                    contentDescription = "Comment",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            commentInfo?.threadId?.let { onNavigateToComments(it) }
                        },
                    contentScale = ContentScale.Inside
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

    var scrubPosition by remember { mutableStateOf<Long?>(null) }
    val displayPosition = scrubPosition ?: currentPosition

    val positionText = remember(displayPosition) {
        Util.getStringForTime(formatBuilder, formatter, displayPosition)
    }
    val durationText = remember(duration) {
        Util.getStringForTime(formatBuilder, formatter, duration)
    }

    val fontB = remember { FontFamily(Font(R.font.b)) }

    val interactionSource = remember { MutableInteractionSource() }
    val isDragged by interactionSource.collectIsDraggedAsState()
    val thumbSize by animateDpAsState(
        targetValue = if (isDragged) 15.dp else 6.dp,
        label = "thumbSize"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(30.dp)
    ) {
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
            Slider(
                value = if (duration > 0) (displayPosition.toFloat() / duration).coerceIn(0f, 1f) else 0f,
                onValueChange = { value ->
                    val newPos = (value * duration).toLong()
                    scrubPosition = newPos
                    onScrubbingChange(true, newPos)
                },
                onValueChangeFinished = {
                    scrubPosition?.let { pos ->
                        if (player?.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM) == true) {
                            player.seekTo(pos)
                        }
                    }
                    scrubPosition = null
                    onScrubbingChange(false, null)
                },
                interactionSource = interactionSource,
                thumb = {
                    Box(
                        modifier = Modifier.size(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(thumbSize)
                                .background(Color.White, CircleShape)
                        )
                    }
                },
                track = {
                    val fraction = if (duration > 0) (displayPosition.toFloat() / duration).coerceIn(0f, 1f) else 0f
                    val bufferedFraction = if (duration > 0) (bufferedPosition.toFloat() / duration).coerceIn(0f, 1f) else 0f

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(1.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = bufferedFraction)
                                    .fillMaxHeight()
                                    .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(1.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = fraction)
                                    .fillMaxHeight()
                                    .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(1.dp))
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .align(Alignment.TopCenter)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .offset(y = 24.dp)
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = positionText,
                color = Color(0xFFD9D9D9),
                fontSize = 11.sp,
                fontFamily = fontB,
                modifier = Modifier.alpha(0.3f)
            )
            Text(
                text = durationText,
                color = Color(0xFFD9D9D9),
                fontSize = 11.sp,
                fontFamily = fontB,
                modifier = Modifier.alpha(0.3f)
            )
        }
    }
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