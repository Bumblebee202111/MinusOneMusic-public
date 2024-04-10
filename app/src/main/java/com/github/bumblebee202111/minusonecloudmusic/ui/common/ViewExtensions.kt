package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.annotation.SuppressLint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.view.View
import android.widget.ImageButton
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.github.bumblebee202111.minusonecloudmusic.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
val Fragment.statusBarHeight: Int
    @SuppressLint("DiscouragedApi", "InternalInsetResource")
    get() {
        var result = 0
        val resourceId: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
val Fragment.mainNavController: NavController
    get() = requireActivity().findNavController(R.id.nav_host_fragment_content_main)

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

fun Toolbar.getNavButtonView(): ImageButton?
  = navButtonViewField.get(this) as? ImageButton

fun Toolbar.setFitHeightNavigationIcon(icon:Drawable){
    val size=resources.getDimensionPixelSize(R.dimen.toolbar_size)
    val bm=icon.toBitmap(size,size)
    setNavigationIcon(BitmapDrawable(resources,bm))
}

private val navButtonViewField = Toolbar::class.java.getDeclaredField("mNavButtonView")
    .also { it.isAccessible = true }


fun View.setBackgroundColorAndTopCorner(@ColorRes color:Int, radius:Float){
    val background =
        ContextCompat.getColor(context, color)
    setBackground(PaintDrawable(background).apply {
        setCornerRadii(floatArrayOf(radius, radius, radius, radius, 0F, 0F, 0F, 0F))
    })
}