package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.CollapsingToolbarLayout

class CustomCollapsingToolbarLayout
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CollapsingToolbarLayout(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (fitsSystemWindows) {
            val mode = MeasureSpec.getMode(heightMeasureSpec)
            val topInset = getTopSystemInset()

            if (mode == MeasureSpec.UNSPECIFIED && topInset > 0) {
                val hasFitsSystemWindowsFlagInChild = (0 until childCount)
                    .map { index -> getChildAt(index) }
                    .any { fitsSystemWindows }
                if (hasFitsSystemWindowsFlagInChild) {
                    val heightSpec = MeasureSpec.makeMeasureSpec(measuredHeight - topInset, MeasureSpec.EXACTLY)
                    super.onMeasure(widthMeasureSpec, heightSpec)
                }
            }
        }
    }

    private fun getTopSystemInset(): Int {
        val field = CollapsingToolbarLayout::class.java.getDeclaredField("lastInsets")
            .apply { isAccessible = true }
        val windowsInsetsCompat = field.get(this) as? WindowInsetsCompat
        return windowsInsetsCompat?.systemWindowInsetTop ?: 0
    }
}