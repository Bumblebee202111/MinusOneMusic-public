package com.github.bumblebee202111.minusonecloudmusic.ui.login


data class LoginResult(
    val success: LoggedInUserView? = null,
    val error: Int? = null
)