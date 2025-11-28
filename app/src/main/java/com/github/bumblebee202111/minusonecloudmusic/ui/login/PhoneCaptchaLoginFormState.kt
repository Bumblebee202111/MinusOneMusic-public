package com.github.bumblebee202111.minusonecloudmusic.ui.login


data class PhoneCaptchaLoginFormState(
    val phoneNumberError: Int? = null,
    val captchaError: Int? = null,
    val isDataValid: Boolean = false
)