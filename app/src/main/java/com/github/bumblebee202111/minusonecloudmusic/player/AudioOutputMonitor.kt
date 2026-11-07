package com.github.bumblebee202111.minusonecloudmusic.player

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioOutputMonitor @Inject constructor(
    @ApplicationContext context: Context
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val mainHandler = Handler(Looper.getMainLooper())

    val isA2dpPlaying: Flow<Boolean> = callbackFlow {
        fun checkA2dp(): Boolean {
            return audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                .any { it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP }
        }

        val callback = object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
                trySend(checkA2dp())
            }

            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
                trySend(checkA2dp())
            }
        }

        trySend(checkA2dp())
        audioManager.registerAudioDeviceCallback(callback, mainHandler)
        awaitClose {
            audioManager.unregisterAudioDeviceCallback(callback)
        }
    }.distinctUntilChanged()

    fun showSystemVolumeUi() {
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_SAME,
            AudioManager.FLAG_SHOW_UI
        )
    }
}