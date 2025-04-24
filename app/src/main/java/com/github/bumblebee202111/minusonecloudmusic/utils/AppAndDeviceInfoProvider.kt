package com.github.bumblebee202111.minusonecloudmusic.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.webkit.WebSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class AppAndDeviceInfoProvider @Inject constructor(@ApplicationContext private val context: Context) {
    private val _deviceId: String by lazy {
        context.deviceId().also {
            Log.d(TAG, "Device ID: $it")
        }
    }

    val screenType: String = determineScreenType(context)

    private val evnsm = "1.0.0"
    private val versioncode = "9002085"
    private val buildver = "250418220822"
    private val os = "android"
    private val channel = "netease"
    private val appver = "9.2.85"

    private val rng: Random by lazy { Random(_deviceId.hashCode()) }
    val deviceId: String by lazy {
        (_deviceId.repeat(78 / _deviceId.length) + _deviceId.take(78 % _deviceId.length)).toCharArray()
            .apply {
                shuffle(rng)
            }.toString()
    }
    val mobilename: String = Build.MODEL ?: UNKNOWN
    val osver: String = Build.VERSION.RELEASE ?: UNKNOWN
    val resolution: String

    init {
        val displayMetrics = context.resources.displayMetrics
        resolution = "${displayMetrics.heightPixels}x${displayMetrics.widthPixels}"
    }

    val apiWNonPersistentCookies: Map<String, String> = mapOf(
        "appver" to "9.0.90",
        COOKIE_NAME_OS to "iPhone OS",
        "osver" to "16.2",
        "channel" to "distribution"
    )
    val eapiNonPersistentCookies: Map<String, String?> = mapOf(
        "EVNSM" to evnsm,
        "versioncode" to versioncode,
        "buildver" to buildver,
        "resolution" to resolution,
        "mobilename" to mobilename,
        "osver" to osver,
        "os" to os,
        COOKIE_NAME_DEVICE_ID to deviceId,
        "appver" to appver,
        "channel" to channel,
        "screenType" to screenType
    )
    val webApiUserAgent: String =
        WebSettings.getDefaultUserAgent(context)
    val eapiUserAgent =
        "NeteaseMusic/$appver.$buildver($versioncode);${System.getProperty("http.agent")}"

    private fun determineScreenType(context: Context): String {
        val config = context.resources.configuration
        val smallestScreenWidthDp = config.smallestScreenWidthDp
        Log.d(
            TAG,            "Determining screen type based on smallestScreenWidthDp: $smallestScreenWidthDp"
        )
        return when {
            smallestScreenWidthDp >= 600 -> "pad"
            else -> "other"
        }.also {
            Log.d(TAG, "ScreenType determined: $it")
        }
    }

    companion object {
        private const val TAG = "AppAndDeviceInfoProvider"
        private const val UNKNOWN = "unknown"
        const val COOKIE_NAME_DEVICE_ID = "deviceId"
        const val COOKIE_NAME_OS = "os"
    }
}

@SuppressLint("HardwareIds")
fun Context.deviceId(): String =
    Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)

