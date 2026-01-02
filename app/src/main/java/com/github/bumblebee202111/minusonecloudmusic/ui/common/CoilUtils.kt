package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.widget.ImageView
import androidx.annotation.ColorInt
import coil3.asDrawable
import coil3.request.ImageResult
import coil3.request.SuccessResult

fun applyDominantColor(
    result: ImageResult,
    targetView: ImageView,
    @ColorInt defaultColor: Int,
    minL: Float,
    maxL: Float
) {
    if (result is SuccessResult) {
        val drawable = result.image.asDrawable(targetView.resources)
        ColorUtils.getDominantColor(
            drawable = drawable,
            defaultColor = defaultColor,
            minL = minL,
            maxL = maxL,
            onGenerated = { color ->
                targetView.setBackgroundColor(color)
            })
    } else {
        targetView.setBackgroundColor(defaultColor)
    }
}