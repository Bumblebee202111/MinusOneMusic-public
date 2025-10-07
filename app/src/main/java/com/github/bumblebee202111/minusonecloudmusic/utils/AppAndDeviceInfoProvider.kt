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

    val mobilename: String = Build.MODEL ?: UNKNOWN
    val osver: String = Build.VERSION.RELEASE ?: UNKNOWN
    val screenType: String by lazy { determineScreenType(context) }
    val resolution: String by lazy {
        val displayMetrics = context.resources.displayMetrics
        "${displayMetrics.heightPixels}x${displayMetrics.widthPixels}"
    }

    
    val deviceId: String by lazy { createShuffledDeviceId(androidId) }

    val webApiUserAgent: String =
        WebSettings.getDefaultUserAgent(context)
    val eapiUserAgent: String by lazy {
        "NeteaseMusic/$APP_VER.$BUILD_VER($VERSION_CODE);${System.getProperty("http.agent")}"
    }

    val eapiNonPersistentCookies: Map<String, String?> by lazy {
        mapOf(
            "EVNSM" to EVNSM,
            "versioncode" to VERSION_CODE,
            "buildver" to BUILD_VER,
            "resolution" to resolution,
            "mobilename" to mobilename,
            "osver" to osver,
            "os" to OS,
            COOKIE_NAME_DEVICE_ID to deviceId,
            "appver" to APP_VER,
            "channel" to CHANNEL,
            "screenType" to screenType
        )
    }

    private val androidId: String by lazy {
        context.getAndroidId().also {
            Log.d(TAG, "Device Android ID: $it")
        }
    }

    private fun createShuffledDeviceId(sourceId: String): String {
        val rng = Random(sourceId.hashCode())
        val repeatedId = sourceId.repeat(78 / sourceId.length) + sourceId.take(78 % sourceId.length)
        return repeatedId.toCharArray().apply { shuffle(rng) }.concatToString()
    }

    private fun determineScreenType(context: Context): String {
        val smallestScreenWidthDp = context.resources.configuration.smallestScreenWidthDp
        return if (smallestScreenWidthDp >= 600) "pad" else "other"
    }

    companion object {
        private const val TAG = "AppAndDeviceInfoProvider"
        private const val UNKNOWN = "unknown"

        private const val EVNSM = "1.0.0"
        private const val VERSION_CODE = "9002097"
        private const val BUILD_VER = "250506155018"
        private const val OS = "android"
        private const val CHANNEL = "netease"
        private const val APP_VER = "9.2.97"

        const val COOKIE_NAME_DEVICE_ID = "deviceId"
        const val COOKIE_NAME_OS = "os"
    }
}

@SuppressLint("HardwareIds")
fun Context.getAndroidId(): String =
    Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)

