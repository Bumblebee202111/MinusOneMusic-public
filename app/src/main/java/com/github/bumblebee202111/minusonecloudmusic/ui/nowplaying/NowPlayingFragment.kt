package com.github.bumblebee202111.minusonecloudmusic.ui.nowplaying

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.Tracks
import androidx.media3.common.util.Assertions
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.common.util.Util.getDrawable
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.PlayerControlView
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentNowPlayingBinding
import com.github.bumblebee202111.minusonecloudmusic.player.RepeatShuffleModeUtil
import com.github.bumblebee202111.minusonecloudmusic.player.RepeatShuffleToggleMode
import com.github.bumblebee202111.minusonecloudmusic.ui.common.LyricsView
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Formatter
import java.util.Locale

@AndroidEntryPoint
@UnstableApi
class NowPlayingFragment : Fragment() {
    lateinit var binding: FragmentNowPlayingBinding
    private val nowPlayingViewModel: NowPlayingViewModel by viewModels()


    private lateinit var resources: Resources
    private lateinit var playerListener: PlayerListener
    private lateinit var artworkView: ImageView
    private lateinit var titleView: TextView
    private lateinit var artistView: TextView
    private lateinit var previousButton: View
    private lateinit var nextButton: View
    private lateinit var playPauseButton: ImageButton
    private lateinit var repeatShuffleToggleButton: ImageButton
    private lateinit var durationView: TextView
    private lateinit var positionView: TextView
    private lateinit var timeBar: DefaultTimeBar
    private lateinit var lyricsView: LyricsView
    private lateinit var formatBuilder: StringBuilder
    private lateinit var formatter: Formatter
    private lateinit var period: Timeline.Period
    private lateinit var window: Timeline.Window
    private lateinit var updateProgressAction: Runnable

    private lateinit var repeatAllShuffleOffButtonDrawable: Drawable
    private lateinit var repeatAllShuffleOnButtonDrawable: Drawable
    private lateinit var repeatOneButtonDrawable: Drawable

    private var player: Player? = null
        set(value) {
            Assertions.checkState(Looper.myLooper() == Looper.getMainLooper())
            Assertions.checkArgument(
                value == null || value.applicationLooper == Looper.getMainLooper()
            )

            if (field === value) {
                return
            }
            field?.removeListener(playerListener)
            lyricsView.setLyrics(null)
            field = value
            value?.addListener(playerListener)
            updateAll()
        }

    private var timeBarMinUpdateIntervalMs: Int = 0

    private lateinit var repeatToggleModes: RepeatShuffleToggleMode
    private var scrubbing: Boolean = false
    private var currentWindowOffset: Long = 0
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var defaultArtwork: Drawable
    private var defaultArtworkId = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentNowPlayingBinding.inflate(inflater, container, false)

        repeatToggleModes = RepeatShuffleToggleMode.REPEAT_SHUFFLE_MODE_ALL_OFF
        timeBarMinUpdateIntervalMs = DEFAULT_TIME_BAR_MIN_UPDATE_INTERVAL_MS
        defaultArtworkId = R.drawable.exo_ic_default_album_image

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()
        artworkView = binding.album
        defaultArtwork = ContextCompat.getDrawable(context, defaultArtworkId)!!

        lyricsView = binding.lyrics


        playerListener = PlayerListener()

        period = Timeline.Period()
        window = Timeline.Window()
        formatBuilder = StringBuilder()
        formatter = Formatter(formatBuilder, Locale.getDefault())
        scrubbing = false
        updateProgressAction = Runnable { updateProgress() }
        currentWindowOffset = 0


        playPauseButton = binding.exoPlayPause.apply {
            setOnClickListener {
                val player = player ?: return@setOnClickListener
                Util.handlePlayPauseButtonAction(player)
            }
        }

        nextButton = binding.exoNext.apply {
            setOnClickListener {
                val player = player ?: return@setOnClickListener
                if (player.isCommandAvailable(Player.COMMAND_SEEK_TO_NEXT)) {
                    player.seekToNext()
                }
            }
        }
        previousButton = binding.exoPrev.apply {
            setOnClickListener {
                val player = player ?: return@setOnClickListener
                if (player.isCommandAvailable(Player.COMMAND_SEEK_TO_NEXT)) {
                    player.seekToPrevious()
                }
            }
        }

        repeatShuffleToggleButton = binding.shuffleRepeat
        repeatShuffleToggleButton.setOnClickListener {
            val player = player ?: return@setOnClickListener
            if (player.isCommandAvailable(Player.COMMAND_SET_REPEAT_MODE) && player.isCommandAvailable(
                    Player.COMMAND_SET_SHUFFLE_MODE
                )
            ) {
                val nextRepeatShuffleToggleMode =
                    RepeatShuffleModeUtil.getNextRepeatShuffleMode(player.repeatMode,player.shuffleModeEnabled)
                player.repeatMode = nextRepeatShuffleToggleMode.repeatMode
                player.shuffleModeEnabled = nextRepeatShuffleToggleMode.shuffleModeEnabled
            }
        }

        resources = getResources()
        repeatAllShuffleOffButtonDrawable =
            getDrawable(context, resources, R.drawable.exo_icon_repeat_all)
        repeatAllShuffleOnButtonDrawable =
            getDrawable(context, resources, R.drawable.exo_icon_shuffle_on)
        repeatOneButtonDrawable = getDrawable(context, resources, R.drawable.exo_icon_repeat_one)

        durationView = binding.duration
        positionView = binding.position
        timeBar = binding.timeBar

        titleView = binding.title
        artistView = binding.artist
        repeatShuffleToggleButton = binding.shuffleRepeat

        updateAll()

        repeatWithViewLifecycle {
            launch {
                nowPlayingViewModel.lyrics.collect(lyricsView::setLyrics)

            }
            launch {
                nowPlayingViewModel.player.collectLatest {
                    player = it
                }
            }

        }


    }


    override fun onStop() {
        super.onStop()
        player=null
    }

    private fun updateNowPlaying() {
        val player = player ?: return
        val mediaMetadata = player.mediaMetadata
        titleView.text = mediaMetadata.title
        artistView.text = mediaMetadata.artist

        val currentMediaItem = player.currentMediaItem
        nowPlayingViewModel.setCurrentSongId(currentMediaItem?.mediaId?.toLong())
    }

    private fun updateForCurrentTrackSelections() {

        val player = player ?: return

        if (player.currentTracks.isEmpty) {
            hideArtwork()
            return
        }
        setTitleAndArtistFromMetadata(player)
        setArtwork(player)


    }

    private fun setTitleAndArtistFromMetadata(player: Player) {
        if (!player.isCommandAvailable(Player.COMMAND_GET_METADATA)) {
            return
        }
        val mediaMetadata = player.mediaMetadata
        titleView.text = mediaMetadata.title
        artistView.text = mediaMetadata.artist
    }


    private fun setArtwork(player: Player) {
        if (setArtworkFromMediaMetadata(player)) {
            return
        }
        if (setDrawableArtwork(defaultArtwork)) {
            return
        }
    }

    private fun setArtworkFromMediaMetadata(player: Player): Boolean {
        if (!player.isCommandAvailable(Player.COMMAND_GET_METADATA)) {
            return false
        }
        val mediaMetadata = player.mediaMetadata
        if (mediaMetadata.artworkData == null) {
            return false
        }
        val bitmap = BitmapFactory.decodeByteArray(
            mediaMetadata.artworkData,  0, mediaMetadata.artworkData!!.size
        )
        return setDrawableArtwork(BitmapDrawable(getResources(), bitmap))
    }

    private fun setDrawableArtwork(drawable: Drawable?): Boolean {
        if (drawable != null) {
            val drawableWidth = drawable.intrinsicWidth
            val drawableHeight = drawable.intrinsicHeight
            if (drawableWidth > 0 && drawableHeight > 0) {
                drawableWidth.toFloat() / drawableHeight
                val scaleStyle = ScaleType.FIT_XY

                artworkView.scaleType = scaleStyle
                artworkView.setImageDrawable(drawable)
                artworkView.visibility = View.VISIBLE
                return true
            }
        }
        return false
    }

    private fun updateAll() {
        updatePlayPauseButton()
        updateRepeatShuffleModeButton()
        updateNowPlaying()
        updateTimeline()
    }

    private fun updatePlayPauseButton() {
        val shouldShowPlayButton = Util.shouldShowPlayButton(player)
        @DrawableRes val drawableRes: Int =
            if (shouldShowPlayButton) R.drawable.exo_ic_play_circle_filled else R.drawable.exo_ic_pause_circle_filled
        playPauseButton.setImageResource(drawableRes)
        val enablePlayPause: Boolean = shouldEnablePlayPauseButton()
        updateButton(enablePlayPause, playPauseButton)
    }

    private fun updateButton(enabled: Boolean, view: View?) {
        if (view == null) {
            return
        }
        view.isEnabled = enabled
    }

    private fun updateRepeatShuffleModeButton() {
        if (!isVisible) {
            return
        }
        val player = player
        if (player == null ||!player.isCommandAvailable(Player.COMMAND_SET_REPEAT_MODE) || !player.isCommandAvailable(
                Player.COMMAND_SET_SHUFFLE_MODE
            )
        ) {
            updateButton( false, repeatShuffleToggleButton)
            return
        }

        updateButton( true, repeatShuffleToggleButton)
        when (player.repeatMode) {
            Player.REPEAT_MODE_ONE -> {
                repeatShuffleToggleButton.setImageDrawable(repeatOneButtonDrawable)
            }
            Player.REPEAT_MODE_ALL -> {
                if (player.shuffleModeEnabled)
                    repeatShuffleToggleButton.setImageDrawable(repeatAllShuffleOnButtonDrawable)
                else
                    repeatShuffleToggleButton.setImageDrawable(repeatAllShuffleOffButtonDrawable)
            }
            else -> {
            }
        }
    }

    private fun updateLyrics() {

    }

    fun updateProgress() {
        if (!isVisible) {
            return
        }
        val player = this.player


        var position: Long = 0
        var bufferedPosition: Long = 0
        if (player != null && player.isCommandAvailable(Player.COMMAND_GET_CURRENT_MEDIA_ITEM)) {
            position = currentWindowOffset + player.contentPosition
            bufferedPosition = currentWindowOffset + player.contentBufferedPosition
        }

        if (!scrubbing) {
            positionView.text = Util.getStringForTime(formatBuilder, formatter, position)
        }

        timeBar.apply {
            setPosition(position)
            setBufferedPosition(bufferedPosition)
        }


        lyricsView.setPosition(player?.currentPosition ?: 0)
        handler.removeCallbacks(updateProgressAction)
        val playbackState = player?.playbackState ?: Player.STATE_IDLE
        if (player != null && player.isPlaying) {
            var mediaTimeDelayMs = timeBar.preferredUpdateDelay
            val mediaTimeUntilNextFullSecondMs = 1000 - position % 1000
            mediaTimeDelayMs = mediaTimeDelayMs.coerceAtMost(mediaTimeUntilNextFullSecondMs)
            val playbackSpeed = player.playbackParameters.speed
            var delayMs =
                if (playbackSpeed > 0) (mediaTimeDelayMs / playbackSpeed).toLong() else Companion.MAX_UPDATE_INTERVAL_MS.toLong()
            delayMs = Util.constrainValue(
                delayMs,
                timeBarMinUpdateIntervalMs.toLong(),
                Companion.MAX_UPDATE_INTERVAL_MS.toLong()
            )
            handler.postDelayed(updateProgressAction, delayMs)
        } else if (playbackState != Player.STATE_ENDED && playbackState != Player.STATE_IDLE) {
            handler.postDelayed(updateProgressAction, Companion.MAX_UPDATE_INTERVAL_MS.toLong())
        }
    }

    fun updateTimeline() {
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
        durationView.text = Util.getStringForTime(formatBuilder, formatter, durationMs)
        timeBar.setDuration(durationMs)
        updateProgress()
        updateLyrics()
    }


    private fun hideArtwork() {
        artworkView.setImageResource(android.R.color.transparent)
        artworkView.visibility = View.INVISIBLE
    }

    private fun shouldEnablePlayPauseButton(): Boolean {
        val player = player ?: return false
        return (player.isCommandAvailable(Player.COMMAND_PLAY_PAUSE) && (!player.isCommandAvailable(
            Player.COMMAND_GET_TIMELINE
        )
                || !player.currentTimeline.isEmpty))
    }

    inner class PlayerListener : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            if (events.containsAny(
                    Player.EVENT_PLAYBACK_STATE_CHANGED,
                    Player.EVENT_PLAY_WHEN_READY_CHANGED,
                    Player.EVENT_IS_PLAYING_CHANGED,
                    Player.EVENT_AVAILABLE_COMMANDS_CHANGED
                )
            ) {
                updateProgress()
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
                    Player.EVENT_PLAYBACK_STATE_CHANGED,
                    Player.EVENT_PLAY_WHEN_READY_CHANGED,
                    Player.EVENT_IS_PLAYING_CHANGED,
                    Player.EVENT_AVAILABLE_COMMANDS_CHANGED
                )
            ) {
                updateProgress()
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
                    Player.EVENT_PLAYBACK_STATE_CHANGED,
                    Player.EVENT_PLAY_WHEN_READY_CHANGED,
                    Player.EVENT_MEDIA_METADATA_CHANGED,
                    Player.EVENT_TIMELINE_CHANGED,
                    Player.EVENT_MEDIA_ITEM_TRANSITION
                )
            )

                updateNowPlaying()
        }

        override fun onTracksChanged(tracks: Tracks) {
            super.onTracksChanged(tracks)
            updateForCurrentTrackSelections()

        }
    }


    companion object {
        private val DEFAULT_REPEAT_SHUFFLE_TOGGLE_MODES: RepeatShuffleToggleMode =
            RepeatShuffleToggleMode.REPEAT_SHUFFLE_MODE_ALL_OFF
        private const val DEFAULT_TIME_BAR_MIN_UPDATE_INTERVAL_MS =
            PlayerControlView.DEFAULT_TIME_BAR_MIN_UPDATE_INTERVAL_MS
        private const val MAX_UPDATE_INTERVAL_MS = 1000
    }
}

