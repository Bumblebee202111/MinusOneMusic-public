package com.github.bumblebee202111.minusonecloudmusic.ui.inbox

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class InboxViewModel @Inject constructor(): ViewModel() {

    private val _text = MutableStateFlow(
        value = "This is Inbox Fragment"
    )
    val text: StateFlow<String> = _text
}