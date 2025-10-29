package com.github.bumblebee202111.minusonecloudmusic.data


sealed class AppError {

    data object Network : AppError()


    data class Server(val code: Int?, val message: String?) : AppError()


    data class Unknown(val exceptionName: String, val exceptionMessage: String) : AppError()
}