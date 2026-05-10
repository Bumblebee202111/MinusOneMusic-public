package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.annotation.SuppressLint
import android.graphics.drawable.PaintDrawable
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils

@SuppressLint("RestrictedApi")
fun View.doOnApplyWindowInsets(f: (v: View, insets: WindowInsetsCompat, padding: ViewPaddingState) -> Unit) {
    val paddingState = createStateForView(this)
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        f(v, insets, paddingState)
        insets
    }
    requestApplyInsetsWhenAttached()
}



fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        requestApplyInsets()
    } else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.requestApplyInsets()
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}

private fun createStateForView(view: View) = ViewPaddingState(
    view.paddingLeft,
    view.paddingTop,
    view.paddingRight,
    view.paddingBottom,
    view.paddingStart,
    view.paddingEnd
)

data class ViewPaddingState(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val start: Int,
    val end: Int
)

fun View.setBackgroundColorAndTopCorner(@ColorRes color: Int, radius: Float) {
    val background =
        ContextCompat.getColor(context, color)
    setBackground(PaintDrawable(background).apply {
        setCornerRadii(floatArrayOf(radius, radius, radius, radius, 0F, 0F, 0F, 0F))
    })
}

@OptIn(ExperimentalBadgeUtils::class)
fun View.attachBadge(badgeDrawable: BadgeDrawable) {
    doOnLayout {
        BadgeUtils.attachBadgeDrawable(badgeDrawable, this)
    }
}