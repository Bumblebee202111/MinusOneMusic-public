package com.github.bumblebee202111.minusonecloudmusic.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.data.Result
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhoneCaptchaLoginViewModel @Inject constructor(private val loginRepository: LoginRepository) : ViewModel() {


    val phoneNumberState = MutableStateFlow<PhoneNumberState?>(null)
    val sendCaptchaResult = MutableStateFlow<SendCaptchaResult?>(null)
    val captchaState = MutableStateFlow<CaptchaState?>(null)


    private val _phoneLoginResult = MutableStateFlow<PhoneLoginResult?>(null)
    val phoneLoginResult: StateFlow<PhoneLoginResult?>
        get() = _phoneLoginResult

    fun sendCaptcha(phoneNumber: String) {
        viewModelScope.launch {
            loginRepository.sendCaptcha(phoneNumber).flowOn(Dispatchers.IO).collect {
                sendCaptchaResult.value = when (it) {
                    is Result.Loading -> SendCaptchaResult.Loading
                    is Result.Success -> SendCaptchaResult.Success
                    is Result.Error -> SendCaptchaResult.Error(it.exception.toString())
                }
            }
        }

    }

    fun login(phoneNumber: String, captcha: String) {
        viewModelScope.launch {
            loginRepository.loginWithCaptcha(phoneNumber = phoneNumber, captcha = captcha).flowOn(Dispatchers.IO)
                .collect { result ->
                    _phoneLoginResult.value = when (result) {
                        is Result.Loading -> {
                            PhoneLoginResult.Loading
                        }

                        is Result.Success -> {
                            PhoneLoginResult.Success
                        }

                        is Result.Error -> {
                            PhoneLoginResult.Error(R.string.login_failed)
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
                loginRepository.verifyPhoneLoginCaptcha(phoneNumber, captcha).collect {
                    when (it) {
                        is Result.Success -> captchaState.value =
                            CaptchaState(isDataValid = true)

                        is Result.Error -> captchaState.value =
                            CaptchaState(error = it.exception.message)

                        else-> {

                        }
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
    data class Error(val errorMsg: String) : SendCaptchaResult
}