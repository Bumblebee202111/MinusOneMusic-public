package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.ColorInt

fun applyDominantColor(
    drawable: Drawable,
    targetView: ImageView,
    @ColorInt defaultColor: Int,
    minL: Float,
    maxL: Float
) {
    ColorUtils.getDominantColor(
        drawable = drawable,
        defaultColor = defaultColor,
        minL = minL,
        maxL = maxL,
        onGenerated = { color ->
            targetView.setBackgroundColor(color)
        }
    )
}