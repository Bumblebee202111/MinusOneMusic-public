package com.github.bumblebee202111.minusonecloudmusic.ui.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToastManager @Inject constructor() {
    private val _uiTextEvent = MutableStateFlow<UiTextEvent?>(null)
    val uiTextEvent = _uiTextEvent.asStateFlow()

    fun showMessage(message: UiText) {
        _uiTextEvent.value = UiTextEvent(message)
    }

    fun onMessageShown() {
        _uiTextEvent.value = null
    }
}