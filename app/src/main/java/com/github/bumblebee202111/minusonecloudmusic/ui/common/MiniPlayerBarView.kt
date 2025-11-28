package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import coil3.load
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.databinding.MiniPlayerBarBinding



@OptIn(UnstableApi::class)
class MiniPlayerBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    playbackAttrs: AttributeSet? = null
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: MiniPlayerBarBinding

    private val playerListener: PlayerListener

    var player: Player? = null
        set(value) {
            if (field === value) {
                return
            }
            field?.removeListener(playerListener)
            field = value
            updateAll()
            value?.addListener(playerListener)
        }

    private var isAttachedToWindow: Boolean = false

    private val period: Timeline.Period
    private val window: Timeline.Window
    private val updateProgressAction: Runnable

    private var showPlayButtonIfSuppressed: Boolean
    private val timeBarMinUpdateIntervalMs: Int


    inner class PlayerListener : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            if (events.containsAny(
                    Player.EVENT_PLAYBACK_STATE_CHANGED,
                    Player.EVENT_PLAY_WHEN_READY_CHANGED,
                    Player.EVENT_AVAILABLE_COMMANDS_CHANGED
                )
            ) {
                updatePlayPauseButtonPlayPause()
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
                    Player.EVENT_POSITION_DISCONTINUITY,
                    Player.EVENT_TIMELINE_CHANGED,
                    Player.EVENT_SEEK_BACK_INCREMENT_CHANGED,
                    Player.EVENT_SEEK_FORWARD_INCREMENT_CHANGED,
                    Player.EVENT_AVAILABLE_COMMANDS_CHANGED
                )
            ) {

            }
            if (events.containsAny(
                    Player.EVENT_POSITION_DISCONTINUITY,
                    Player.EVENT_TIMELINE_CHANGED,
                    Player.EVENT_AVAILABLE_COMMANDS_CHANGED
                )
            ) {
                updatePlayPauseButtonTimeline()
            }
            if (events.containsAny(
                    Player.EVENT_TRACKS_CHANGED,
                    Player.EVENT_AVAILABLE_COMMANDS_CHANGED
                )
            ) {
                updateForCurrentTrackSelections()
            }
        }
    }

    val DEFAULT_TIME_BAR_MIN_UPDATE_INTERVAL_MS = 200
    private val MAX_UPDATE_INTERVAL_MS = 1000

    init {
        var layoutId = R.layout.mini_player_bar
        showPlayButtonIfSuppressed = true
        timeBarMinUpdateIntervalMs = DEFAULT_TIME_BAR_MIN_UPDATE_INTERVAL_MS
        if (playbackAttrs != null) {
            val a = context
                .theme
                .obtainStyledAttributes(
                    playbackAttrs, R.styleable.MiniPlayerBarView, defStyleAttr,  0
                )
            try {
                layoutId = a.getResourceId(R.styleable.MiniPlayerBarView_layout_id, layoutId)
            } finally {
                a.recycle()
            }

        }

        binding = MiniPlayerBarBinding.inflate(LayoutInflater.from(context), this)
        orientation = VERTICAL



        playerListener = PlayerListener()
        period = Timeline.Period()
        window = Timeline.Window()
        updateProgressAction = Runnable { updateProgress() }

        binding.playPauseButton.setOnClickListener {
            Util.handlePlayPauseButtonAction(player, showPlayButtonIfSuppressed)
        }
    }

    private fun updateAll() {
        updatePlayPauseButtonPlayPause()
        updatePlayPauseButtonTimeline()
        updateProgress()
        updateForCurrentTrackSelections()
    }

    fun updateForCurrentTrackSelections() {
        val player = player ?: return
        if (!player.isCommandAvailable(Player.COMMAND_GET_TRACKS)
        ) {
            return
        }

        val mediaMetadata = player.mediaMetadata

        binding.songInfoLayout.cover.load(mediaMetadata.artworkUri ?: mediaMetadata.artworkData)

        binding.songInfoLayout.tvMusic.setTitleAndArtist(
            mediaMetadata.title.toString(),
            mediaMetadata.artist.toString()
        )
    }


    private fun updatePlayPauseButtonPlayPause() {
        if (!isVisible() || !isAttachedToWindow) {
            return
        }

        val shouldShowPlayButton = Util.shouldShowPlayButton(player, showPlayButtonIfSuppressed)
        binding.playPauseButton.isPlaying = !shouldShowPlayButton
        val enablePlayPause: Boolean = shouldEnablePlayPauseButton()
        updateButton(enablePlayPause, binding.playPauseButton)
    }

    private fun updatePlayPauseButtonTimeline() {
        val player = player ?: return
        var durationUs: Long = 0
        val timeline =
            if (player.isCommandAvailable(Player.COMMAND_GET_TIMELINE)) player.currentTimeline else Timeline.EMPTY
        if (!timeline.isEmpty) {
            val currentWindowIndex = player.currentMediaItemIndex
            timeline.getWindow(currentWindowIndex, window)
            timeline.getPeriod(0, period)
            durationUs += window.durationUs
        }
        val durationMs = Util.usToMs(durationUs)
        binding.playPauseButton.max = durationMs.toInt()
        updateProgress()
    }

    private fun updateProgress() {
        if (!isVisible() || !isAttachedToWindow) {
            return
        }
        val player = player
        var position: Long = 0
        if (player != null && player.isCommandAvailable(Player.COMMAND_GET_CURRENT_MEDIA_ITEM)) {
            position = player.contentPosition
        }

        binding.playPauseButton.setProgress(position.toInt())


        removeCallbacks(updateProgressAction)
        val playbackState = player?.playbackState ?: Player.STATE_IDLE
        if (player != null && player.isPlaying) {
            var mediaTimeDelayMs =
                MAX_UPDATE_INTERVAL_MS.toLong()


            val mediaTimeUntilNextFullSecondMs = 1000 - position % 1000
            mediaTimeDelayMs = mediaTimeDelayMs.coerceAtMost(mediaTimeUntilNextFullSecondMs)


            val playbackSpeed = player.playbackParameters.speed
            var delayMs =
                if (playbackSpeed > 0) (mediaTimeDelayMs / playbackSpeed).toLong() else MAX_UPDATE_INTERVAL_MS.toLong()


            delayMs = Util.constrainValue(
                delayMs,
                timeBarMinUpdateIntervalMs.toLong(),
                MAX_UPDATE_INTERVAL_MS.toLong()
            )
            postDelayed(updateProgressAction, delayMs)
        } else if (playbackState != Player.STATE_ENDED && playbackState != Player.STATE_IDLE) {
            postDelayed(updateProgressAction, MAX_UPDATE_INTERVAL_MS.toLong())
        }
    }

    fun isVisible(): Boolean {
        return visibility == VISIBLE
    }

    fun setPlaylistButtonListener(onClickListener: OnClickListener?) {
        binding.playlistButton.apply {
            setOnClickListener(onClickListener)
            updateButton(onClickListener != null, this)
        }
    }

    private fun updateButton(enabled: Boolean, view: View?) {
        if (view == null) {
            return
        }
        view.isEnabled = enabled
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isAttachedToWindow = true
        updateAll()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isAttachedToWindow = false
        removeCallbacks(updateProgressAction)
    }

    private fun shouldEnablePlayPauseButton(): Boolean {
        val player = player ?: return false
        return (player.isCommandAvailable(Player.COMMAND_PLAY_PAUSE) && (!player.isCommandAvailable(
            Player.COMMAND_GET_TIMELINE
        )
                || !player.currentTimeline.isEmpty))
    }


}
