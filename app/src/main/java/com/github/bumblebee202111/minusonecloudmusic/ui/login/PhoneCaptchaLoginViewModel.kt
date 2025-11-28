package com.github.bumblebee202111.minusonecloudmusic.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.data.AppResult
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoginRepository
import com.github.bumblebee202111.minusonecloudmusic.ui.common.ToastManager
import com.github.bumblebee202111.minusonecloudmusic.ui.common.UiText
import com.github.bumblebee202111.minusonecloudmusic.ui.mapper.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhoneCaptchaLoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val toastManager: ToastManager
) : ViewModel() {


    val phoneNumberState = MutableStateFlow<PhoneNumberState?>(null)
    val sendCaptchaResult = MutableStateFlow<SendCaptchaResult?>(null)
    val captchaState = MutableStateFlow<CaptchaState?>(null)


    private val _phoneLoginResult = MutableStateFlow<PhoneLoginResult?>(null)
    val phoneLoginResult: StateFlow<PhoneLoginResult?>
        get() = _phoneLoginResult

    fun sendCaptcha(phoneNumber: String) {
        viewModelScope.launch {
            loginRepository.sendCaptcha(phoneNumber).flowOn(Dispatchers.IO).collect { result ->
                sendCaptchaResult.value = when (result) {
                    is AppResult.Loading -> SendCaptchaResult.Loading
                    is AppResult.Success -> SendCaptchaResult.Success
                    is AppResult.Error -> {
                        toastManager.showMessage(result.error.toUiText())
                        SendCaptchaResult.Error
                    }
                }
            }
        }

    }

    fun login(phoneNumber: String, captcha: String) {
        viewModelScope.launch {
            loginRepository.loginWithCaptcha(phoneNumber = phoneNumber, captcha = captcha)
                .flowOn(Dispatchers.IO)
                .collect { result ->
                    _phoneLoginResult.value = when (result) {
                        is AppResult.Loading -> PhoneLoginResult.Loading

                        is AppResult.Success -> {
                            toastManager.showMessage(UiText.StringResource(R.string.welcome))
                            PhoneLoginResult.Success
                        }

                        is AppResult.Error -> {
                            toastManager.showMessage(result.error.toUiText())
                            PhoneLoginResult.Error
                        }
                    }
                }
        }

    }

    fun phoneNumberChanged(phoneNumber: String) {
        if (isPhoneNumberValid(phoneNumber)) {
            phoneNumberState.value = PhoneNumberState(isDataValid = true)
        } else {
            phoneNumberState.value = PhoneNumberState(R.string.invalid_phone_number)
        }
    }

    fun captchaChanged(phoneNumber: String, captcha: String) {
        if (isCaptchaValid(captcha)) {
            viewModelScope.launch {
                loginRepository.verifyPhoneLoginCaptcha(phoneNumber, captcha).collect { result ->
                    when (result) {
                        is AppResult.Success -> captchaState.value =
                            CaptchaState(isDataValid = true)

                        is AppResult.Error -> {
                            toastManager.showMessage(result.error.toUiText())
                            captchaState.value = CaptchaState(isDataValid = false)
                        }
                        is AppResult.Loading -> {  }
                    }

                }
            }

        } else {
            captchaState.value = CaptchaState()
        }
    }


    private fun isPhoneNumberValid(phoneNumber: String): Boolean {
        return phoneNumber.length == 11
    }


    private fun isCaptchaValid(captcha: String): Boolean {
        return captcha.length == 6
    }
}

class PhoneNumberState(
    val error: Int? = null,
    val isDataValid: Boolean = false
)

class CaptchaState(
    val error: String? = null,
    val isDataValid: Boolean = false
)

sealed interface SendCaptchaResult {
    object Loading : SendCaptchaResult
    object Success : SendCaptchaResult
    object Error : SendCaptchaResult
}