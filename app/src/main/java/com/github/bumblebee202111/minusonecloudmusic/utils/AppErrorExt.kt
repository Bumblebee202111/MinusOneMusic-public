package com.github.bumblebee202111.minusonecloudmusic.utils

import android.util.Log
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.data.AppError
import java.io.IOException


fun AppError.toUiText(): UiText {
    return when (this) {
        is AppError.Network -> {
            UiText.StringResource(R.string.error_network_unavailable)
        }
        is AppError.ApiError -> {
            val hasMessage = !message.isNullOrBlank()
            val hasCode = code != null
            when {
                hasMessage && hasCode -> {
                    UiText.StringResourceWithArgs(
                        R.string.error_api_with_message_and_code,
                        listOf(message, code)
                    )
                }
                hasMessage -> {
                    UiText.DynamicString(message)
                }
                hasCode -> {
                    UiText.StringResourceWithArgs(
                        R.string.error_api_with_code,
                        listOf(code)
                    )
                }
                else -> {
                    UiText.StringResource(R.string.error_api_generic)
                }
            }
        }
        is AppError.Unknown -> {
            UiText.StringResourceWithArgs(
                R.string.error_unexpected_with_details,
                listOf(exceptionName, exceptionMessage)
            )
        }
    }
}

fun Throwable.toAppError(): AppError {
    Log.e("ApiFlow", "An unexpected error occurred in a flow", this)
    return when (this) {
        is IOException -> AppError.Network
        else -> AppError.Unknown(this::class.java.simpleName, this.message ?: "No message")
    }
}