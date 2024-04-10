package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

fun RequestManager.loadWithPlaceholder(
    imageUrl: String? = null,
    placeholder: Drawable? = null
): RequestBuilder<Drawable> {
    val rb = load(imageUrl ?: placeholder)

    if (imageUrl != null) rb.run { placeholder(placeholder) }
    return rb
}

fun RequestManager.loadWithPlaceholder(
    imageUrl: String? = null,
    @DrawableRes placeholder: Int
): RequestBuilder<Drawable> {
    val rb = if(imageUrl!=null)load(imageUrl )else load( placeholder)

    if (imageUrl != null) rb.run { placeholder(placeholder) }
    return rb
}
fun ImageView.loadImageUrl(
    imageUrl: String?,
    placeholder: Drawable? = null,
    circleCrop: Boolean? = false,
    centerCrop: Boolean? = false
) : RequestBuilder<Drawable>{

    val rb = Glide.with(context).load(imageUrl ?: placeholder)

    if (placeholder!=null) rb.run { placeholder(placeholder) }
    if (circleCrop == true) rb.run { optionalCircleCrop() }
    if (centerCrop == true) rb.run { optionalCenterCrop() }
    return rb
}

fun ImageView.loadImageUrl(
    imageUrl: String?,
    @DrawableRes placeholder: Int? = null,
    circleCrop: Boolean? = false,
    centerCrop: Boolean? = false
): RequestBuilder<Drawable> {

    val rb = Glide.with(context).load(imageUrl ?: placeholder)

    if (placeholder!=null) rb.run { placeholder(placeholder) }
    if (circleCrop == true) rb.run { optionalCircleCrop() }
    if (centerCrop == true) rb.run { optionalCenterCrop() }
    return rb
}

fun RequestBuilder<Drawable>.extractDominantColor(into: ImageView, @ColorInt withDefaultColor:Int, minL: Float,
                                                  maxL: Float  ): RequestBuilder<Drawable> {
    return addListener(
        object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }

            override fun onResourceReady(
                resource: Drawable,
                model: Any,
                target: Target<Drawable>?,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {

                ColorUtils.getDominantColor(
                    resource, withDefaultColor,minL,maxL
                ) { into.setBackgroundColor(this) }

                return false
            }

        }
    )
}