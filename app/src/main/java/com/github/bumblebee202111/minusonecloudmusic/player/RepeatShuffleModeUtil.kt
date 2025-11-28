
package com.github.bumblebee202111.minusonecloudmusic.player

import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi



@UnstableApi
object RepeatShuffleModeUtil {

    fun getNextRepeatShuffleMode(
        currentRepeatMode: @Player.RepeatMode Int,
        currentShuffleEnabled: Boolean
    ): RepeatShuffleToggleMode {
        val currentRepeatShuffleMode =
            RepeatShuffleToggleMode.entries.first { it.repeatMode == currentRepeatMode && it.shuffleModeEnabled == currentShuffleEnabled }
        return RepeatShuffleToggleMode.entries[(currentRepeatShuffleMode.ordinal + 1) % RepeatShuffleToggleMode.entries.size]
    }

}

