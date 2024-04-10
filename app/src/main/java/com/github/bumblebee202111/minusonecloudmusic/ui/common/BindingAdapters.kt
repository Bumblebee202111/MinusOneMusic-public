package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.data.model.Billboard
import com.github.bumblebee202111.minusonecloudmusic.ui.toplists.BillboardAdapter
import kotlin.math.roundToInt



@BindingAdapter("isGone")
fun View.setIsGone(
    isGone: Boolean
) {
    visibility = if (isGone) View.GONE else View.VISIBLE
}

@BindingAdapter(
    value = ["imageUrl", "circleCrop", "placeholder", "crossFadeDuration"],
    requireAll = false
)
fun ImageView.imageUrl(
    imageUrl: String?,
    circleCrop: Boolean? = false,
    placeholder: Drawable? = null,
    crossFadeDuration: Int? = null
) {
    if (imageUrl == null && placeholder == null) return

    var rb = Glide.with(context).load(imageUrl ?: placeholder)
    with(rb) {
        if (imageUrl != null && placeholder != null) {
            rb = placeholder(placeholder)
        }
        if (circleCrop == true) {
            rb = optionalCircleCrop()
        }
        if (crossFadeDuration != null) {
            rb = transition(DrawableTransitionOptions.withCrossFade(crossFadeDuration))
        }
        into(this@imageUrl)
    }

}


@BindingAdapter("android:text")
fun setText(view: TextView, @StringRes resId: Int) {
    if (resId == 0) {
        view.text = null
    } else {
        view.setText(resId)
    }
}
@BindingAdapter(
    value = ["imageUrl", "circleCrop", "placeholder", "crossFadeDuration"],
    requireAll = false
)
fun ImageView.imageUrl(
    imageUrl: String?,
    circleCrop: Boolean? = false,
    placeholder: String? = null,
    crossFadeDuration: Int? = null
) {
    imageUrl(
        imageUrl,
        circleCrop,
        placeholder?.let { ColorDrawable(Color.parseColor(it)) },
        crossFadeDuration
    )
}


@BindingAdapter("billboards")
fun bindBillboards(view: RecyclerView, billboards: List<Billboard>?) {
    if (!billboards.isNullOrEmpty()) {
        view.isVisible = true
        val adapter = (view.adapter as? BillboardAdapter ?: BillboardAdapter())
        view.adapter = adapter
        adapter.submitList(billboards)
    } else {
        view.isGone = true
    }
}

@BindingAdapter("artists")
fun TextView.artists(artists: List<String>?) {
    text = artists?.joinToString("/") ?: "Unknown"
}

@BindingAdapter(value = ["songItemSubtitleArtists", "songItemSubtitleAlbum"], requireAll = false)
fun TextView.playlistSongSubtitle(
    songItemSubtitleArtists: List<String>?,
    songItemSubtitleAlbum: String?
) {
    val sb = StringBuilder()
    val artistsString =
        songItemSubtitleArtists?.joinToString("/") ?: "Unknown"
    sb.append(artistsString)

    sb.append(" - ")

    val albumString = songItemSubtitleAlbum ?: "Unknown"
    sb.append(albumString)

    text = sb.toString()
}

@BindingAdapter(
    value = ["playerSongItemTitle", "playerSongItemArtists", "playerSongItemIsCurrentSong"],
    requireAll = false
)
fun TextView.playerSongItem(
    songItemTitle: String?,
    songItemArtists: List<String>?,
    isCurrentSong: Boolean
) {
    val titleString = songItemTitle ?: "Unknown"
    val artistsString = songItemArtists?.joinToString("/") ?: "Unknown"
    val spannableString = SpannableString("$titleString · $artistsString")
    val titleLength = titleString.length
    spannableString.setSpan(
        AbsoluteSizeSpan(ViewUtils.dpToPx(context, 15.0f).roundToInt()),
        0,
        titleLength,
        33
    )
    spannableString.setSpan(
        AbsoluteSizeSpan(ViewUtils.dpToPx(context, 11.0f).roundToInt()),
        titleLength,
        spannableString.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    spannableString.setSpan(
        ForegroundColorSpan(
            if (isCurrentSong) ContextCompat.getColor(
                context,
                R.color.colorPrimary1
            ) else Color.BLACK
        ),
        0,
        titleLength,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    text = spannableString
    setTextColor(
        if (isCurrentSong) ContextCompat.getColor(
            context,
            R.color.colorPrimary1
        ) else Color.parseColor("#7f000000")
    )
}

@BindingAdapter("isCurrentSong")
fun TextView.setIsCurrentSong(isCurrentSong: Boolean) {
    setTextColor(
        if (isCurrentSong) ContextCompat.getColor(
            context,
            R.color.colorPrimary1
        ) else Color.parseColor("#CC000000")
    )
}

@BindingAdapter("isCurrentSong")
fun LinearLayout.setIsCurrentSong(isCurrentSong: Boolean) {
    if (isCurrentSong)
        setBackgroundResource(R.color.colorText7)
    else setBackgroundColor(Color.WHITE)
}

@BindingAdapter("app:navigationIcon")
fun Toolbar.bindNavigationIcon(icon: Drawable) {
    setFitHeightNavigationIcon(icon)
}