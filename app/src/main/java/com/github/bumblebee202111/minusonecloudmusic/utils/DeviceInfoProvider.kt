package com.github.bumblebee202111.minusonecloudmusic.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Cookie
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class DeviceInfoProvider @Inject constructor(@ApplicationContext context: Context) {
    private val _deviceId: String by lazy { context.deviceId() }

    private val rng: Random by lazy { Random(_deviceId.hashCode()) }
    val deviceId: String by lazy {
        (_deviceId.repeat(78 / _deviceId.length) + _deviceId.take(78 % _deviceId.length)).toCharArray()
            .shuffle(rng).toString()
    }
    val pcDeviceInfoCookies: Map<String, String>
        get() {

            return mapOf(
                "appver" to pcAppVersions.random(rng),
                COOKIE_NAME_OS to "pc"
            )
        }
    val androidDeviceInfoCookies: Map<String, String?>
        get() {
            val androidModelCandidate= androidModelCandicates.random(rng)
            return mapOf(
                "versioncode" to "9000080",
                "buildver" to "240522204909",
                "resolution" to androidModelCandidate[ "resolution" ],
                "mobilename" to androidModelCandidate["mobilename"],
                "osver" to androidOsVersionCandidates.random(rng),
                "os" to "android",
                COOKIE_NAME_DEVICE_ID to deviceId,
                "appver" to "9.0.80",
                "channel" to "netease",
                "screenType" to androidModelCandidate["screenType"],
                "sDeviceId" to deviceId
            )
        }
    val pcUserAgent: String
        get() = allUserAgents.filter { it.contains("Macintosh") || it.contains("Windows") }
            .random(rng)
    val androidUserAgent: String
        get() {
            return allUserAgents.filter { it.contains("Android") }
                .random(rng)
        }
    companion object {
        const val COOKIE_NAME_DEVICE_ID = "deviceId"
        const val COOKIE_NAME_OS = "os"
        val pcAppVersions = listOf("2.7.1.198277", "2.7.0.198228")
        val androidModelCandicates= listOf(
            mapOf("resolution" to "1104x1920","mobilename" to "MIPAD4","screenType" to "pad"),
             mapOf("resolution" to "2030x1080","mobilename" to "RedmiNote5"),
            mapOf("resolution" to "2269x1080","mobilename" to "2112123AG"),
        )
        val androidOsVersionCandidates = listOf("8.1.0", "12")

        val myDeviceInfoCookies = listOf(
            "versioncode=9000080",
            "buildver=240522204909",
            "resolution=1104x1920",
            "mobilename=MIPAD4",
            "osver=8.1.0",
            "os=android",
            "$COOKIE_NAME_DEVICE_ID=bnVsbAkxODowMTpmMTo0Zjo2ODo1MQk4OGRhM2JkYzVlMThjMzM2CWMxYjRjZGU1YjlmNmQ2NTc%3D",
            "appver=9.0.20",
            "channel=netease",
            "screenType=pad",
            "sDeviceId=bnVsbAkxODowMTpmMTo0Zjo2ODo1MQk4OGRhM2JkYzVlMThjMzM2CWMxYjRjZGU1YjlmNmQ2NTc%3D"
        )

        private val bujuanUserAgents = listOf(
            "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1",
            "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Mobile Safari/537.36",
            "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Mobile Safari/537.36",
            "Mozilla/5.0 (Linux; Android 5.1.1; Nexus 6 Build/LYZ28E) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Mobile Safari/537.36",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_2 like Mac OS X) AppleWebKit/603.2.4 (KHTML, like Gecko) Mobile/14F89;GameHelper",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 10_0 like Mac OS X) AppleWebKit/602.1.38 (KHTML, like Gecko) Version/10.0 Mobile/14A300 Safari/602.1",
            "Mozilla/5.0 (iPad; CPU OS 10_0 like Mac OS X) AppleWebKit/602.1.38 (KHTML, like Gecko) Version/10.0 Mobile/14A300 Safari/602.1",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.12; rv:46.0) Gecko/20100101 Firefox/46.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_5) AppleWebKit/603.2.4 (KHTML, like Gecko) Version/10.1.1 Safari/603.2.4",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:46.0) Gecko/20100101 Firefox/46.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/13.10586"
        )

        private val androidUserAgent =
            "NeteaseMusic/9.0.80.240522204909(9000080);Dalvik/2.1.0 (Linux; U; Android 8.1.0; MI PAD 4 MIUI/V10.3.2.0.ODJCNXM)"

        val allUserAgents = bujuanUserAgents + androidUserAgent


    }
}

fun buildCookie(name: String, value: String): Cookie {
    return Cookie.Builder().name(name).value(value).path("/").domain("music.163.com").build()
}

@SuppressLint("HardwareIds")
fun Context.deviceId(): String =
    Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)

