package com.github.bumblebee202111.minusonecloudmusic.utils

import androidx.core.net.toUri

fun String.imageUrl(
    imageView: Int = 1,
    thumbnailSize: Int?,
    type: String = "webp",
    quality: Int? = null
): String {
    return toUri().buildUpon().appendQueryParameter("imageView", imageView.toString())
        .run {
            thumbnailSize?.let {
                appendQueryParameter("thumbnail", "${it}z${it}")
            } ?: this
        }
        .appendQueryParameter("type", type)
        .run {
            quality?.let {
                appendQueryParameter("quality", it.toString())
            } ?: this
        }
        .toString()
}