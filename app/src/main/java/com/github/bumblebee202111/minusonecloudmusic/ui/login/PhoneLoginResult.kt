package com.github.bumblebee202111.minusonecloudmusic.ui.login

import androidx.annotation.StringRes


sealed interface PhoneLoginResult{
    data object Loading:PhoneLoginResult
    data object Success:PhoneLoginResult
    data object Error:PhoneLoginResult
}