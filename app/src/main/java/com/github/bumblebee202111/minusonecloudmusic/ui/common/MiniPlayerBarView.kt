package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.content.Context
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.Assertions
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import com.bumptech.glide.Glide
import com.github.bumblebee202111.minusonecloudmusic.R



@UnstableApi
class MiniPlayerBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    playbackAttrs: AttributeSet? = null
) : LinearLayout(context, attrs, defStyleAttr) {

    private val playerListener: PlayerListener

    var player: Player? = null
        set(value) {
            Assertions.checkState(Looper.myLooper() == Looper.getMainLooper())
            Assertions.checkArgument(
                value == null || value.applicationLooper == Looper.getMainLooper()
            )
            if (field === value) {
                return
            }
            field?.removeListener(playerListener)
            field = value
            updateForCurrentTrackSelections( true)

            value?.addListener(playerListener)
            updateAll()
        }

    private var isAttachedToWindow: Boolean = false

    private var playPauseButton: MiniPlayPauseButton? = null
    private var artworkView: ImageView? = null
    private var titleAndArtistView: MiniBarTextView? = null
    private var playlistButton: ImageView? = null

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
                    Player.EVENT_AVAILABLE_COMMANDS_CHANGED
                )
            ) {
            }
            if (events.containsAny(
                    Player.EVENT_SHUFFLE_MODE_ENABLED_CHANGED,
                    Player.EVENT_AVAILABLE_COMMANDS_CHANGED
                )
            ) {
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
                    Player.EVENT_PLAYBACK_PARAMETERS_CHANGED,
                    Player.EVENT_AVAILABLE_COMMANDS_CHANGED
                )
            ) {
            }
            if (events.containsAny(
                    Player.EVENT_TRACKS_CHANGED,
                    Player.EVENT_AVAILABLE_COMMANDS_CHANGED
                )
            ) {
                updateForCurrentTrackSelections(true)
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

        LayoutInflater.from(context).inflate(layoutId,  this)
        orientation = VERTICAL
        val v = findViewById<View>(R.id.v3ShadowView).apply {
        }

        playerListener = PlayerListener()
        period = Timeline.Period()
        window = Timeline.Window()
        updateProgressAction = Runnable { updateProgress() }

        playPauseButton = findViewById(R.id.play_pause_button)
        playPauseButton?.setOnClickListener {
            Util.handlePlayPauseButtonAction(player, showPlayButtonIfSuppressed)
        }

        artworkView = findViewById(R.id.cover)
        titleAndArtistView = findViewById(R.id.tv_music)
        playlistButton = findViewById(R.id.playlist_button)
    }

    private fun updateAll() {
        updatePlayPauseButtonPlayPause()
        updatePlayPauseButtonTimeline()
    }

    fun updateForCurrentTrackSelections(isNewPlayer: Boolean) {
        val player = player ?: return
        if (!player.isCommandAvailable(Player.COMMAND_GET_TRACKS)
        ) {
            return
        }

        val mediaMetadata = player.mediaMetadata

        artworkView?.let { artworkView ->
            Glide.with(artworkView.context)
                .load(mediaMetadata.artworkUri ?: mediaMetadata.artworkData).into(artworkView)
        }

        titleAndArtistView?.setTitleAndArtist(
            mediaMetadata.title.toString(),
            mediaMetadata.artist.toString()
        )
    }


    private fun updatePlayPauseButtonPlayPause() {
        if (!isVisible() || !isAttachedToWindow) {
            return
        }
        playPauseButton?.let { playPauseButton ->
            val shouldShowPlayButton = Util.shouldShowPlayButton(player, showPlayButtonIfSuppressed)
            playPauseButton.isPlaying = !shouldShowPlayButton
            val enablePlayPause: Boolean = shouldEnablePlayPauseButton()
            updateButton(enablePlayPause, playPauseButton)
        }
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
        playPauseButton?.max = durationMs.toInt()
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

        playPauseButton?.setProgress(position.toInt())
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
        playlistButton?.apply {
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
