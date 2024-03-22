package com.github.bumblebee202111.minusonecloudmusic.ui.login

import androidx.annotation.StringRes


sealed interface PhoneLoginResult{
    object Loading:PhoneLoginResult
    object Success:PhoneLoginResult
    class Error(@StringRes val errorMsgResId:Int):PhoneLoginResult
}