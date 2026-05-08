package com.github.bumblebee202111.minusonecloudmusic.ui.nowplaying

import android.Manifest
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.DrawableRes
import androidx.annotation.OptIn
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.ui.PlayerControlView
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
import com.github.bumblebee202111.minusonecloudmusic.databinding.LayoutNowPlayingContentBinding
import com.github.bumblebee202111.minusonecloudmusic.model.CommentInfo
import com.github.bumblebee202111.minusonecloudmusic.model.LyricsEntry
import com.github.bumblebee202111.minusonecloudmusic.model.RemoteSong
import com.github.bumblebee202111.minusonecloudmusic.player.CountUtil
import com.github.bumblebee202111.minusonecloudmusic.player.RepeatShuffleModeUtil
import com.github.bumblebee202111.minusonecloudmusic.player.RepeatShuffleToggleMode
import com.github.bumblebee202111.minusonecloudmusic.ui.common.ViewUtils
import com.github.bumblebee202111.minusonecloudmusic.ui.common.attachBadge
import com.github.bumblebee202111.minusonecloudmusic.ui.common.doOnApplyWindowInsets
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.google.common.base.Preconditions
import java.util.Formatter
import java.util.Locale

@OptIn(UnstableApi::class, ExperimentalBadgeUtils::class)
class NowPlayingView(
    context: Context,
    private val onNavigateBack: () -> Unit,
    private val onNavigateToComments: (String) -> Unit,
    private val onOpenPlaylist: () -> Unit,
    private val onLikeClicked: () -> Unit,
    private val onDownloadClick: () -> Unit,
    private val requestPermissionLauncher: ActivityResultLauncher<String>?
) : FrameLayout(context) {

    private val binding: LayoutNowPlayingContentBinding =
        LayoutNowPlayingContentBinding.inflate(LayoutInflater.from(context), this, true)

    private var playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            if (events.containsAny(
                    Player.EVENT_PLAYBACK_STATE_CHANGED,
                    Player.EVENT_PLAY_WHEN_READY_CHANGED,
                    Player.EVENT_IS_PLAYING_CHANGED,
                    Player.EVENT_AVAILABLE_COMMANDS_CHANGED
                )
            ) {
                updateProgress()
                updateLyrics()
            }
            if (events.containsAny(
                    Player.EVENT_PLAYBACK_STATE_CHANGED,
                    Player.EVENT_PLAY_WHEN_READY_CHANGED,
                    Player.EVENT_AVAILABLE_COMMANDS_CHANGED
                )
            ) {
                updatePlayPauseButton()
            }

            if (events.containsAny(
                    Player.EVENT_REPEAT_MODE_CHANGED,
                    Player.EVENT_SHUFFLE_MODE_ENABLED_CHANGED,
                    Player.EVENT_AVAILABLE_COMMANDS_CHANGED
                )
            ) {
                updateRepeatShuffleModeButton()
            }

            if (events.containsAny(
                    Player.EVENT_POSITION_DISCONTINUITY,
                    Player.EVENT_TIMELINE_CHANGED,
                    Player.EVENT_AVAILABLE_COMMANDS_CHANGED
                )
            ) {
                updateTimeline()
            }

            if (events.containsAny(
                    Player.EVENT_PLAY_WHEN_READY_CHANGED,
                    Player.EVENT_MEDIA_METADATA_CHANGED,
                    Player.EVENT_MEDIA_ITEM_TRANSITION
                )
            )
                updateMediaMetadata()
        }
    }

    private var formatBuilder: StringBuilder = StringBuilder()
    private var formatter: Formatter = Formatter(formatBuilder, Locale.getDefault())
    private var period: Timeline.Period = Timeline.Period()
    private var window: Timeline.Window = Timeline.Window()
    private val updateProgressAction = Runnable(::updateProgress)
    private val updateLyricsAction = Runnable(::updateLyrics)

    private val repeatAllShuffleOffButtonDrawable: Drawable =
        Util.getDrawable(context, resources, R.drawable.ic_full_screen_player_repeat_all_shuffle_disabled)!!
    private val repeatAllShuffleOnButtonDrawable: Drawable =
        Util.getDrawable(context, resources, R.drawable.ic_full_screen_player_repeat_all_shuffle_enabled)!!
    private val repeatOneButtonDrawable: Drawable =
        Util.getDrawable(context, resources, R.drawable.ic_full_screen_player_repeat_one)!!

    private val commentWithoutCountDrawable: Drawable = Util.getDrawable(context, resources, R.drawable.hbb)!!
    private val commentWithCountDrawable: Drawable = Util.getDrawable(context, resources, R.drawable.hbi)!!

    private val notLikedWithoutCountDrawable: Drawable = Util.getDrawable(context, resources, R.drawable.hct)!!
    private val notLikedWithCountDrawable: Drawable = Util.getDrawable(context, resources, R.drawable.hco)!!
    private val likedWithoutCountDrawable: Drawable = Util.getDrawable(context, resources, R.drawable.hcx)!!
    private val likedWithCountDrawable: Drawable = Util.getDrawable(context, resources, R.drawable.hcv)!!

    private val lyricsModeNotLiked: Int = R.drawable.h_o
    private val lyricsModeLiked: Int = R.drawable.h_q

    private var songTitle: String? = null
    private var songArtist: String? = null
    private var playlistName: String? = "music"

    private val audioManager = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private lateinit var audioDeviceCallback: AudioDeviceCallback

    var player: Player? = null
        set(value) {
            Preconditions.checkState(Looper.myLooper() == Looper.getMainLooper())
            Preconditions.checkArgument(
                value == null || value.applicationLooper == Looper.getMainLooper()
            )

            if (field === value) {
                return
            }
            field?.removeListener(playerListener)
            field = value
            value?.addListener(playerListener)
            updateAll()
        }

    private var timeBarMinUpdateIntervalMs = TIME_BAR_MIN_UPDATE_INTERVAL_MS

    private var isLyricsMode: Boolean = false
    private var scrubbing: Boolean = false
    private val handler = Handler(Looper.getMainLooper())

    private val defaultArtworkId = R.drawable.h_7
    private val defaultArtwork: Drawable = ContextCompat.getDrawable(context, defaultArtworkId)!!

    private val likeBadge = createBadgeDrawable(context)
    private val commentBadge = createBadgeDrawable(context)

    init {
        setupViews()
    }

    private fun setupViews() {
        binding.lyrics.setOnClickListener {
            isLyricsMode = !isLyricsMode
            updateUiMode()
        }

        binding.artistImageContainer.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                isLyricsMode = !isLyricsMode
                updateUiMode()
            }
        }
        binding.modeLyricsLayout.visibility = View.GONE

        binding.playerPlayPause.setOnClickListener {
            val player = player ?: return@setOnClickListener
            Util.handlePlayPauseButtonAction(player)
        }

        binding.playerNext.setOnClickListener {
            val player = player ?: return@setOnClickListener
            if (player.isCommandAvailable(Player.COMMAND_SEEK_TO_NEXT)) {
                player.seekToNextMediaItem()
                Util.handlePlayButtonAction(player)
            }
        }

        binding.playerPrev.setOnClickListener {
            val player = player ?: return@setOnClickListener
            if (player.isCommandAvailable(Player.COMMAND_SEEK_TO_NEXT)) {
                player.seekToPreviousMediaItem()
                Util.handlePlayButtonAction(player)
            }
        }

        binding.openPlaylist.setOnClickListener { onOpenPlaylist() }

        audioDeviceCallback = object : AudioDeviceCallback() {
            var audioDevices: List<AudioDeviceInfo> = emptyList()
            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
                super.onAudioDevicesAdded(addedDevices)
                if (addedDevices != null) audioDevices += addedDevices
                updateIcon()
            }

            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
                super.onAudioDevicesRemoved(removedDevices)
                if (removedDevices != null) audioDevices -= removedDevices
                updateIcon()
            }

            fun updateIcon() {
                val isA2dpPlaying = audioDevices.any { it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP }
                binding.deviceBtnStyle1.setImageResource(if (isA2dpPlaying) R.drawable.f5j else R.drawable.f5i)
            }
        }
        audioManager.registerAudioDeviceCallback(audioDeviceCallback, handler)

        binding.deviceBtnStyle1.setOnClickListener {
            if (!SystemOutputSwitcherDialogController.showDialog(context)) {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_SAME,
                    AudioManager.FLAG_SHOW_UI
                )
            }
        }

        binding.downloadButton.setOnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && requestPermissionLauncher != null) {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                onDownloadClick()
            }
        }

        val likeClickListener = View.OnClickListener { onLikeClicked() }

        binding.likeButton.apply {
            attachBadge(likeBadge)
            setOnClickListener(likeClickListener)
        }

        binding.commentButton.apply {
            attachBadge(commentBadge)
        }

        binding.lyricLikeBtn.setOnClickListener(likeClickListener)

        binding.playerShuffleRepeat.setOnClickListener {
            val player = player ?: return@setOnClickListener
            if (player.isCommandAvailable(Player.COMMAND_SET_REPEAT_MODE) && player.isCommandAvailable(
                    Player.COMMAND_SET_SHUFFLE_MODE
                )
            ) {
                val nextRepeatShuffleToggleMode =
                    RepeatShuffleModeUtil.getNextRepeatShuffleMode(
                        player.repeatMode,
                        player.shuffleModeEnabled
                    )
                player.repeatMode = nextRepeatShuffleToggleMode.repeatMode
                player.shuffleModeEnabled = nextRepeatShuffleToggleMode.shuffleModeEnabled
            }
        }

        binding.timeBar.addListener(object : TimeBar.OnScrubListener {
            override fun onScrubStart(timeBar: TimeBar, position: Long) {
                scrubbing = true
                binding.position.text = Util.getStringForTime(formatBuilder, formatter, position)
            }

            override fun onScrubMove(timeBar: TimeBar, position: Long) {
                binding.position.text = Util.getStringForTime(formatBuilder, formatter, position)
            }

            override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
                scrubbing = false
                val player = player ?: return
                if (!canceled) {
                    seekToTimeBarPosition(player, position)
                }
            }
        })

        binding.toolbar.setNavigationOnClickListener { onNavigateBack() }

        binding.root.doOnApplyWindowInsets { _, insets, _ ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.statusBar.run {
                layoutParams.height = systemBars.top
                isVisible = layoutParams.height > 0
                requestLayout()
            }
        }
    }

    fun updateState(
        currentSong: Any?,
        lyrics: List<LyricsEntry>?,
        likeState: LikeState,
        commentInfo: CommentInfo?,
        isLoggedIn: Boolean
    ) {
        val downloadable = (currentSong as? RemoteSong?)?.isDownloadable == true
        binding.downloadButton.isVisible = downloadable
        ConstraintSet().apply {
            clone(binding.playBottomContainer)
            if (downloadable) {
                setHorizontalBias(R.id.deviceBtnStyle1, 1 / 6f)
                setHorizontalBias(R.id.more_button, 5 / 6f)
            } else {
                setHorizontalBias(R.id.deviceBtnStyle1, 1 / 4f)
                setHorizontalBias(R.id.more_button, 3 / 4f)
            }
            applyTo(binding.playBottomContainer)
        }

        if (lyrics != null) {
            binding.lyrics.setLyrics(lyrics)
        }

        with(likeState) {
            likeBadge.text = likeCountDisplayText
            if (like == true) {
                if (likeCountDisplayText != null) {
                    binding.likeButton.setImageDrawable(likedWithCountDrawable)
                } else {
                    binding.likeButton.setImageDrawable(likedWithoutCountDrawable)
                }
                binding.lyricLikeBtn.setImageResource(lyricsModeLiked)
            } else {
                if (likeCountDisplayText != null) {
                    binding.likeButton.setImageDrawable(notLikedWithCountDrawable)
                } else {
                    binding.likeButton.setImageDrawable(notLikedWithoutCountDrawable)
                }
                binding.lyricLikeBtn.setImageResource(lyricsModeNotLiked)
            }
        }

        commentBadge.text = commentInfo?.commentCount?.let(CountUtil::getAbbreviatedCommentCount) ?: ""
        if (commentInfo != null) {
            binding.commentButton.setImageDrawable(commentWithCountDrawable)
            binding.commentButton.setOnClickListener { onNavigateToComments(commentInfo.threadId) }
        } else {
            binding.commentButton.setImageDrawable(commentWithoutCountDrawable)
            binding.commentButton.setOnClickListener(null)
        }

        binding.likeButton.isEnabled = isLoggedIn
        binding.lyricLikeBtn.isEnabled = isLoggedIn
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        player = null
        handler.removeCallbacksAndMessages(null)
        try {
            audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
        } catch (e: Exception) {
        }
    }

    private fun updateMediaMetadata() {
        val player = player ?: return
        if (!player.isCommandAvailable(Player.COMMAND_GET_METADATA)) {
            return
        }
        val mediaMetadata = player.mediaMetadata
        songTitle = mediaMetadata.title?.toString()
        songArtist = mediaMetadata.artist?.toString()
        updateUiMode()

        binding.songActionsTitle.text = mediaMetadata.title
        binding.songActionsArtistName.text = mediaMetadata.artist

        val defaultBgColor = ContextCompat.getColor(binding.smallAlbumCover0.context, R.color.default_player_background)

        binding.smallAlbumCover0.load(mediaMetadata.artworkUri ?: mediaMetadata.artworkData) {
            placeholder(defaultArtwork)
            error(defaultArtwork)
            transformations(CircleCropTransformation())
            allowHardware(false)

            target(
                onError = { error ->
                    binding.smallAlbumCover0.setImageDrawable(error?.asDrawable(resources))
                    binding.bigAlbumCover.setBackgroundColor(defaultBgColor)
                },
                onSuccess = { result ->
                    binding.smallAlbumCover0.setImageDrawable(result.asDrawable(resources))

                    if (result is BitmapImage) {
                        val bitmap = result.bitmap
                        Palette.from(bitmap).generate { palette ->
                            val dominantColor = palette?.getDominantColor(defaultBgColor) ?: defaultBgColor
                            val hsl = FloatArray(3)
                            ColorUtils.colorToHSL(dominantColor, hsl)
                            hsl[2] = hsl[2].coerceIn(0.15F, 0.45F)
                            binding.bigAlbumCover.setBackgroundColor(Color.HSVToColor(hsl))
                        }
                    } else {
                        binding.bigAlbumCover.setBackgroundColor(defaultBgColor)
                    }
                }
            )
        }
    }

    private fun updateAll() {
        updatePlayPauseButton()
        updateRepeatShuffleModeButton()
        updateMediaMetadata()
        updateTimeline()
    }

    private fun updatePlayPauseButton() {
        val shouldShowPlayButton = Util.shouldShowPlayButton(player)
        @DrawableRes val drawableRes: Int =
            if (shouldShowPlayButton) R.drawable.ic_full_screen_player_play else R.drawable.ic_full_screen_player_pause
        binding.playerPlayPause.setImageResource(drawableRes)
        val enablePlayPause: Boolean = shouldEnablePlayPauseButton()
        updateButton(enablePlayPause, binding.playerPlayPause)
    }

    private fun updateButton(enabled: Boolean, view: View?) {
        view?.isEnabled = enabled
    }

    private fun seekToTimeBarPosition(player: Player, positionMs: Long) {
        if (player.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)) {
            player.seekTo(positionMs)
        }
        updateProgress()
        updateLyrics()
    }

    private fun updateRepeatShuffleModeButton() {
        val player = player
        if (player == null || !player.isCommandAvailable(Player.COMMAND_SET_REPEAT_MODE) || !player.isCommandAvailable(
                Player.COMMAND_SET_SHUFFLE_MODE
            )
        ) {
            updateButton(false, binding.playerShuffleRepeat)
            return
        }

        updateButton(true, binding.playerShuffleRepeat)
        when (player.repeatMode) {
            Player.REPEAT_MODE_ONE -> binding.playerShuffleRepeat.setImageDrawable(repeatOneButtonDrawable)
            Player.REPEAT_MODE_ALL -> {
                if (player.shuffleModeEnabled)
                    binding.playerShuffleRepeat.setImageDrawable(repeatAllShuffleOnButtonDrawable)
                else
                    binding.playerShuffleRepeat.setImageDrawable(repeatAllShuffleOffButtonDrawable)
            }
            else -> {}
        }
    }

    private fun updateLyrics() {
        val player = this.player

        var position: Long = 0
        if (player != null && player.isCommandAvailable(Player.COMMAND_GET_CURRENT_MEDIA_ITEM)) {
            position = player.contentPosition
        }

        binding.lyrics.setPosition(position)

        handler.removeCallbacks(updateLyricsAction)
        val playbackState = player?.playbackState ?: Player.STATE_IDLE
        if (player != null && player.isPlaying) {
            handler.postDelayed(updateLyricsAction, LYRICS_MIN_UPDATE_INTERVAL_MS.toLong())
        } else if (playbackState != Player.STATE_ENDED && playbackState != Player.STATE_IDLE) {
            handler.postDelayed(updateLyricsAction, LYRICS_MAX_UPDATE_INTERVAL_MS.toLong())
        }
    }

    private fun updateProgress() {
        val player = this.player

        var position: Long = 0
        var bufferedPosition: Long = 0
        if (player != null && player.isCommandAvailable(Player.COMMAND_GET_CURRENT_MEDIA_ITEM)) {
            position = player.contentPosition
            bufferedPosition = player.contentBufferedPosition
        }

        if (!scrubbing) {
            binding.position.text = Util.getStringForTime(formatBuilder, formatter, position)
        }

        binding.timeBar.run {
            setPosition(position)
            setBufferedPosition(bufferedPosition)
        }

        handler.removeCallbacks(updateProgressAction)
        val playbackState = player?.playbackState ?: Player.STATE_IDLE
        if (player != null && player.isPlaying) {
            var mediaTimeDelayMs = binding.timeBar.preferredUpdateDelay
            val mediaTimeUntilNextFullSecondMs = 1000 - position % 1000
            mediaTimeDelayMs = mediaTimeDelayMs.coerceAtMost(mediaTimeUntilNextFullSecondMs)

            val playbackSpeed = player.playbackParameters.speed
            var delayMs =
                if (playbackSpeed > 0) (mediaTimeDelayMs / playbackSpeed).toLong() else TIME_BAR_MAX_UPDATE_INTERVAL_MS.toLong()

            delayMs = Util.constrainValue(
                delayMs,
                timeBarMinUpdateIntervalMs.toLong(),
                TIME_BAR_MAX_UPDATE_INTERVAL_MS.toLong()
            )
            handler.postDelayed(updateProgressAction, delayMs)
        } else if (playbackState != Player.STATE_ENDED && playbackState != Player.STATE_IDLE) {
            handler.postDelayed(updateProgressAction, TIME_BAR_MAX_UPDATE_INTERVAL_MS.toLong())
        }
    }

    private fun updateTimeline() {
        val player = this.player ?: return

        var durationUs: Long = 0
        val timeline =
            if (player.isCommandAvailable(Player.COMMAND_GET_TIMELINE)) player.currentTimeline else Timeline.EMPTY
        if (!timeline.isEmpty) {
            val currentWindowIndex = player.currentMediaItemIndex
            timeline.getWindow(currentWindowIndex, window)
            for (j in window.firstPeriodIndex..window.lastPeriodIndex) {
                timeline.getPeriod(j, period)
            }
            durationUs += window.durationUs
        } else {
            val playerDurationMs = player.contentDuration
            if (playerDurationMs != C.TIME_UNSET) {
                durationUs = Util.msToUs(playerDurationMs)
            }
        }
        val durationMs = Util.usToMs(durationUs)
        binding.duration.text = Util.getStringForTime(formatBuilder, formatter, durationMs)
        binding.timeBar.setDuration(durationMs)
        updateProgress()
        updateLyrics()
    }

    private fun shouldEnablePlayPauseButton(): Boolean {
        val player = player ?: return false
        return (player.isCommandAvailable(Player.COMMAND_PLAY_PAUSE) && (!player.isCommandAvailable(
            Player.COMMAND_GET_TIMELINE
        )
                || !player.currentTimeline.isEmpty))
    }

    private fun updateUiMode() {
        binding.artistImageContainer.isVisible = !isLyricsMode
        binding.songActionsLayout.isVisible = !isLyricsMode
        binding.modeLyricsLayout.isVisible = isLyricsMode
        if (isLyricsMode) {
            binding.toolbar.title = songTitle.orEmpty()
            binding.toolbar.subtitle = songArtist.orEmpty()
            updateLyrics()
        } else {
            binding.toolbar.title = null
            binding.toolbar.subtitle = playlistName.orEmpty()
        }
    }

    private fun createBadgeDrawable(context: Context): BadgeDrawable =
        BadgeDrawable.create(context).apply {
            isVisible = true
            setTextAppearance(R.style.TextAppearance_App_Player_Badge)
            horizontalOffset = ViewUtils.dpToPx(context, 12).toInt()
            verticalOffset = ViewUtils.dpToPx(context, 11).toInt()
            backgroundColor = Color.TRANSPARENT
        }

    companion object {
        private const val TIME_BAR_MIN_UPDATE_INTERVAL_MS =
            PlayerControlView.DEFAULT_TIME_BAR_MIN_UPDATE_INTERVAL_MS
        private const val TIME_BAR_MAX_UPDATE_INTERVAL_MS = 1000

        const val LYRICS_MIN_UPDATE_INTERVAL_MS = 200
        const val LYRICS_MAX_UPDATE_INTERVAL_MS = 500
    }
}