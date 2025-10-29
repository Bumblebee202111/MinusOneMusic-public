package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper
import android.graphics.drawable.StateListDrawable
import android.util.StateSet
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.github.bumblebee202111.minusonecloudmusic.R

object BottomNavigationIconsUtils {
    fun getBottomNavigationIcons(context: Context): Map<Int, Drawable> {
        val resources = context.resources
        return destinationIconResIdsMap.mapValues { iconResIds ->
            val originalActiveIcon =
                ContextCompat.getDrawable(context,iconResIds.value.activeResId)
            val activeIcon = BottomNavigationActiveIconDrawable(
                originalActiveIcon, context
            )
            val inactiveIcon =
                ResourcesCompat.getDrawable(resources, iconResIds.value.inactiveResId, null)

            StateListDrawable().apply {
                addState(
                    intArrayOf(android.R.attr.state_checked), activeIcon
                )
                addState(
                    StateSet.WILD_CARD, inactiveIcon
                )
            }

        }
    }

    private class BottomNavigationStateIconResIds(
        @DrawableRes val activeResId: Int, @DrawableRes val inactiveResId: Int
    )

    private class BottomNavigationActiveIconDrawable(
        drawable: Drawable?, private val context: Context
    ) : DrawableWrapper(drawable) {
        private lateinit var xfermode: PorterDuffXfermode
        private lateinit var shader: LinearGradient
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val color0 = context.getColor(R.color.colorSecondary1_1)
        private val color1 = context.getColor(R.color.colorPrimary1)

        override fun draw(canvas: Canvas) {
            paint.apply {
                xfermode = null
                shader = null
            }
            val savedLayer = canvas.saveLayer(
                bounds.left.toFloat(),
                bounds.top.toFloat(),
                bounds.right.toFloat(),
                bounds.bottom.toFloat(),
                paint
            )
            super.draw(canvas)
            paint.apply {
                xfermode =this@BottomNavigationActiveIconDrawable.xfermode
                shader = this@BottomNavigationActiveIconDrawable.shader
            }
            val size = bounds.width().coerceAtMost(bounds.height())
            val radius =
                (size / 2F - ViewUtils.dpToPx(context, 1.0F)).coerceAtLeast(0.0F)
            canvas.drawCircle(bounds.exactCenterX(), bounds.exactCenterY(), radius, paint)
            canvas.restoreToCount(savedLayer)
        }

        override fun onBoundsChange(bounds: Rect) {
            super.onBoundsChange(bounds)
            this.shader = LinearGradient(
                 bounds.width() * 0.35f,
                 bounds.height() * 0.65f,
                 bounds.width() * 0.85f,
                 bounds.top.toFloat(),
                 this.color0,
                 this.color1,
                 Shader.TileMode.CLAMP
            )
            this.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_ATOP)
        }
    }

    private val destinationIconResIdsMap = mapOf(
        R.id.nav_wow to BottomNavigationStateIconResIds(
            activeResId = R.drawable.a5t,
            inactiveResId = R.drawable.a5s
        ),
        R.id.nav_user_track to BottomNavigationStateIconResIds(
            activeResId = R.drawable.aap,
            inactiveResId = R.drawable.aao
        ),
        R.id.nav_mine to BottomNavigationStateIconResIds(
            activeResId = R.drawable.bkf,
            inactiveResId = R.drawable.bke
        )
    )
}