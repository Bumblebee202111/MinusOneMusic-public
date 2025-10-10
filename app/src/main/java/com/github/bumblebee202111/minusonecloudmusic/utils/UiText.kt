package com.github.bumblebee202111.minusonecloudmusic.utils

import android.content.Context
import androidx.annotation.StringRes

sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    data class StringResource(@StringRes val resId: Int) : UiText()
    data class StringResourceWithArgs(
        @StringRes val resId: Int,
        val args: List<Any>
    ) : UiText()

    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> context.getString(resId)
            is StringResourceWithArgs -> context.getString(resId, *args.toTypedArray())
        }
    }
}