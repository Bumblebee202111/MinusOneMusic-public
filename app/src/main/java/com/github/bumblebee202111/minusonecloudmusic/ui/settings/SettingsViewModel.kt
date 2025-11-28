package com.github.bumblebee202111.minusonecloudmusic.ui.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(): ViewModel() {

    private val _text = MutableStateFlow(
        value = "This is settings Fragment")
    val text: StateFlow<String> get()  = _text
}