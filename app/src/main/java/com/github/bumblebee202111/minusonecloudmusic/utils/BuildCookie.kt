package com.github.bumblebee202111.minusonecloudmusic.utils

import okhttp3.Cookie

fun buildCommonCookie(name: String, value: String): Cookie {
    return Cookie.Builder().name(name).value(value).path("/").domain("music.163.com").build()
}
fun buildEapiCookie(name: String, value: String): Cookie {
    return Cookie.Builder().name(name).value(value).path("/eapi/").domain("music.163.com").build()
}