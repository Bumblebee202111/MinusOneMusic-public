package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.PaintDrawable
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorRes
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

inline fun Fragment.repeatWithViewLifecycle(
    minState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    if (minState == Lifecycle.State.INITIALIZED || minState == Lifecycle.State.DESTROYED) {
        throw IllegalArgumentException("minState must be between INITIALIZED and DESTROYED")
    }
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.lifecycle.repeatOnLifecycle(minState) {
            block()
        }
    }
}

fun Fragment.requireFragment(id: Int): Fragment {
    return childFragmentManager.findFragmentById(id) ?: throw IllegalArgumentException()
}

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

context (Fragment)
fun View.hideSoftInput() {
    val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.hideSoftInputFromWindow(windowToken, 0)
}

@OptIn(ExperimentalBadgeUtils::class)
fun View.attachBadge(badgeDrawable: BadgeDrawable) {
    doOnLayout {
        BadgeUtils.attachBadgeDrawable(badgeDrawable, this)
    }
}