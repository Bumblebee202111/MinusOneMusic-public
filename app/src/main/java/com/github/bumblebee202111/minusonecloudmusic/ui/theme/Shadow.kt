package com.github.bumblebee202111.minusonecloudmusic.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


data class DolphinShadow(
    val offsetX: Dp = 0.dp,
    val offsetY: Dp = 0.dp,
    val blurRadius: Dp = 0.dp,
    val color: Color = Color.Transparent
)


fun parseDolphinShadow(shadowString: String): DolphinShadow {
    val values = shadowString
        .removePrefix("shadow(")
        .removeSuffix(")")
        .split(',')

    if (values.size < 4) return DolphinShadow()

    val offsetX = values[0].toFloatOrNull()?.dp ?: 0.dp
    val offsetY = values[1].toFloatOrNull()?.dp ?: 0.dp
    val blur = values[2].toFloatOrNull()?.dp ?: 0.dp
    val color = rgbaToColor(colorString)

    return DolphinShadow(offsetX, offsetY, blur, color)
}

