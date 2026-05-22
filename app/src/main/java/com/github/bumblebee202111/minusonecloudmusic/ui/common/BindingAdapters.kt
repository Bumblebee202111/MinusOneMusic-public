package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import coil3.load
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import coil3.request.transformations
import coil3.transform.CircleCropTransformation
import com.github.bumblebee202111.minusonecloudmusic.utils.imageUrl

fun ImageView.loadImage(
    model: Any?,
    thumbnailSize: Int? = null,
    quality: Int? = null,
    circleCrop: Boolean? = false,
    placeholder: Any? = null,
    crossFadeDuration: Int? = null
) {
    val placeholderDrawable: Drawable? = when (placeholder) {
        is Drawable -> placeholder
        is Int -> try {
            ResourcesCompat.getDrawable(resources,placeholder,null)
        } catch (_: Exception) { null }
        is String -> try {
            placeholder.toColorInt().toDrawable()
        } catch (_: Exception) { null }
        else -> null
    }

    val dataToLoad = if (model is String) {
        model.imageUrl(thumbnailSize = thumbnailSize, quality = quality)
    } else {
        model
    }

    if (dataToLoad == null && placeholderDrawable == null) {
        this.setImageDrawable(null)
        return
    }

    load(dataToLoad) {
        if (placeholderDrawable != null) {
            placeholder(placeholderDrawable)
            error(placeholderDrawable)
        }
        if (circleCrop == true) {
            transformations(CircleCropTransformation())
        }
        if (crossFadeDuration != null) {
            crossfade(crossFadeDuration)
        } else {
            crossfade(true)
        }
    }
}