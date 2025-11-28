package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.content.Context
import android.util.TypedValue
import androidx.annotation.Dimension
import androidx.annotation.Dimension.Companion.DP

object ViewUtils {
    fun dpToPx(context: Context, @Dimension(unit = DP) dp: Int): Float {
        return dpToPx(context,dp.toFloat())
    }
    fun dpToPx(context: Context, @Dimension(unit = DP) dp: Float): Float {
        val r = context.resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            r.displayMetrics
        )
    }
}