package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette

object ColorUtils {
    fun getDominantColor(
        drawable: Drawable,
        @ColorInt defaultColor: Int,
        minL: Float,
        maxL: Float,
        onGenerated: (color: Int) -> Unit
    ) {
        Palette.from((drawable as BitmapDrawable).bitmap).generate { palette ->
            val dominantColor = palette?.getDominantColor(
                defaultColor
            )
                ?: defaultColor
            val hsl = FloatArray(3)
            ColorUtils.colorToHSL(dominantColor, hsl)

            hsl[2] = hsl[2].coerceIn(minL, maxL)
            onGenerated(Color.HSVToColor(hsl))
        }
    }
}