package com.github.bumblebee202111.minusonecloudmusic.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.login.LoginQrCodeStatus
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoginRepository
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QrCodeLoginViewModel @Inject constructor(private val loginRepository: LoginRepository) : ViewModel() {
    val loginQrCode: StateFlow<String?>

    private val _loginQrCodeStatus: MutableStateFlow<LoginQrCodeStatus?> = MutableStateFlow(null)
    val loginQrCodeStatus
        get():StateFlow<LoginQrCodeStatus?> = _loginQrCodeStatus

    init {
        val loginQrCodeResult = loginRepository.getLoginQrCode().onEach {
            if (it.data != null) {
                viewModelScope.launch {
                    loginRepository.getLoginQrCodeStatus().flowOn(Dispatchers.IO)
                        .collect { result ->
                            _loginQrCodeStatus.value = result.data
                        }
                }

            }
        }.flowOn(Dispatchers.IO)

        loginQrCode = loginQrCodeResult.map {
            it.data
        }.stateInUi()

    }

    private val _loginResult = MutableStateFlow<LoginResult?>(null)
    val loginResult: StateFlow<LoginResult?> get() = _loginResult


}
