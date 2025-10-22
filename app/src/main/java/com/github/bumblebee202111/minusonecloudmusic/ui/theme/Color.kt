package com.github.bumblebee202111.minusonecloudmusic.ui.theme
import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt


fun rgbaToColor(rgbaString: String): Color {
    val values = rgbaString
        .removeSuffix(")")
        .split(',')
        .mapNotNull { it.trim().toFloatOrNull() }

    if (values.size != 4) return Color.Unspecified

    val red = values[0].toInt()
    val green = values[1].toInt()
    val blue = values[2].toInt()
    val alpha = values[3]

    return Color(red, green, blue, (alpha * 255).roundToInt())
}


