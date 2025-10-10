package com.github.bumblebee202111.minusonecloudmusic.data


sealed class AppResult<out T> {

    data class Success<T>(override val data: T) : AppResult<T>()
    data class Error<T>(val error: AppError,override val data:T?=null) : AppResult<T>()

    data class Loading<T>(override val data: T? = null) : AppResult<T>()

    abstract val data:T?
}

