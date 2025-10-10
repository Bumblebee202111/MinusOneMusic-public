package com.github.bumblebee202111.minusonecloudmusic.utils

data class UiTextEvent(
    val message: UiText,
    private val id: Long = System.nanoTime()
)