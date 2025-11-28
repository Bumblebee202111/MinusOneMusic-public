package com.github.bumblebee202111.minusonecloudmusic.player

import androidx.media3.common.Player

@Suppress("unused")
enum class RepeatShuffleToggleMode(
    val repeatMode: @Player.RepeatMode Int,
    val shuffleModeEnabled: Boolean
) {
    REPEAT_SHUFFLE_MODE_ALL_OFF(Player.REPEAT_MODE_ALL, false),
    REPEAT_SHUFFLE_MODE_ALL_ON(Player.REPEAT_MODE_ALL, true),
    REPEAT_SHUFFLE_MODE_ONE_ON(Player.REPEAT_MODE_ONE, false)
}