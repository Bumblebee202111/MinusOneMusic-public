package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.databinding.MiniPlayerBarBinding

class MusicPlayerBarPlayerListener(
    private val player: Player,
    private val binding: MiniPlayerBarBinding
) : Player.Listener {

    private val playPauseButton = binding.playPauseButton
    private val artworkView = null

    @UnstableApi
    override fun onEvents(player: Player, events: Player.Events) {
        if (events.containsAny(
                Player.EVENT_PLAYBACK_STATE_CHANGED,
                Player.EVENT_PLAY_WHEN_READY_CHANGED,
                Player.EVENT_AVAILABLE_COMMANDS_CHANGED
            )
        ) {
            updatePlayPauseButton()
        }
    }

    override fun onTracksChanged(tracks: Tracks) {
        super.onTracksChanged(tracks)
        updateForCurrentTrackSelections()
    }

    @UnstableApi
    private fun updatePlayPauseButton() {
        val shouldShowPlayButton = Util.shouldShowPlayButton(player)
        @DrawableRes val drawableRes: Int =
            if (shouldShowPlayButton) R.drawable.exo_ic_play_circle_filled else R.drawable.exo_ic_pause_circle_filled
        playPauseButton
            .setImageResource(drawableRes)
    }

    private fun updateForCurrentTrackSelections() {
        if (player.currentTracks.isEmpty) {
            return
        }
        if (setArtworkFromMediaMetadata(player)) {
            return
        }

        val defaultArtwork: Drawable? = null
        if (setDrawableArtwork(defaultArtwork)) {
            return
        }

    }

    private fun setArtworkFromMediaMetadata(player: Player): Boolean {
        val mediaMetadata = player.mediaMetadata
        if (mediaMetadata.artworkData == null) {
            return false
        }
        val bitmap = BitmapFactory.decodeByteArray(
            mediaMetadata.artworkData,  0, mediaMetadata.artworkData!!.size
        )
        return setDrawableArtwork(BitmapDrawable(binding.root.resources, bitmap))
    }

    private fun setDrawableArtwork(drawable: Drawable?): Boolean {
        if (drawable != null) {
            val drawableWidth = drawable.intrinsicWidth
            val drawableHeight = drawable.intrinsicHeight
            if (drawableWidth > 0 && drawableHeight > 0) {
                return true
            }
        }
        return false
    }
    @UnstableApi
    fun updateAll() {
        updatePlayPauseButton()
    }
}

