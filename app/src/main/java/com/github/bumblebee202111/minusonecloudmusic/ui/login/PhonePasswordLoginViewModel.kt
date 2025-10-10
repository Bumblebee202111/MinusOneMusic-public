package com.github.bumblebee202111.minusonecloudmusic.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.data.AppResult
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoginRepository
import com.github.bumblebee202111.minusonecloudmusic.utils.ToastManager
import com.github.bumblebee202111.minusonecloudmusic.utils.UiText
import com.github.bumblebee202111.minusonecloudmusic.utils.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhonePasswordLoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val toastManager: ToastManager
) :
    ViewModel() {

    val loginFormState = MutableStateFlow<LoginFormState?>(null)

    private val _phoneLoginResult = MutableStateFlow<PhoneLoginResult?>(null)
    val phoneLoginResult: StateFlow<PhoneLoginResult?> get() = _phoneLoginResult


    fun login(phoneNumber: String, password: String) {
        viewModelScope.launch {
            loginRepository.loginWithPassword(phoneNumber = phoneNumber, password = password)
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

    fun loginDataChanged(phoneNumber: String, password: String) {
        if (!isPhoneNumberValid(phoneNumber)) {
            loginFormState.value = LoginFormState(R.string.invalid_phone_number)
        } else if (!isPasswordValid(password)) {
            loginFormState.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            loginFormState.value = LoginFormState(isDataValid = true)
        }
    }
    private fun isPhoneNumberValid(phoneNumber: String): Boolean {
        return phoneNumber.length == 11
    }
    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }

    class LoginFormState(
        val phoneNumberError: Int? = null,
        val passwordError: Int? = null,
        val isDataValid: Boolean = false
    )

}

