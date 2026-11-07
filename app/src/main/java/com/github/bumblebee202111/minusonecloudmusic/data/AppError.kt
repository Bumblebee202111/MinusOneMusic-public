package com.github.bumblebee202111.minusonecloudmusic.data

import android.util.Log
import java.io.IOException


sealed class AppError {
    
    data object Network : AppError()

    
    data class Server(val code: Int?, val message: String?) : AppError()

    
    data class Unknown(val exceptionName: String, val exceptionMessage: String) : AppError()
}

fun Throwable.toAppError(): AppError {
    Log.e("ApiFlow", "An unexpected error occurred in a flow", this)
    return when (this) {
        is IOException -> AppError.Network
        else -> AppError.Unknown(this::class.java.simpleName, this.message ?: "No message")
    }
}