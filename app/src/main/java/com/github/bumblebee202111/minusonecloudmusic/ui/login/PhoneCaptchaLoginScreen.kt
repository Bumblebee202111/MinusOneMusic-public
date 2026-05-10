package com.github.bumblebee202111.minusonecloudmusic.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bumblebee202111.minusonecloudmusic.R

@Composable
fun PhoneCaptchaLoginScreen(
    onNavigateToPasswordLogin: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: PhoneCaptchaLoginViewModel = hiltViewModel()
) {
    var phoneNumber by remember { mutableStateOf("") }
    var captcha by remember { mutableStateOf("") }

    val phoneNumberState by viewModel.phoneNumberState.collectAsStateWithLifecycle()
    val sendCaptchaResult by viewModel.sendCaptchaResult.collectAsStateWithLifecycle()
    val captchaState by viewModel.captchaState.collectAsStateWithLifecycle()
    val loginResult by viewModel.phoneLoginResult.collectAsStateWithLifecycle()

    val isCaptchaSent = sendCaptchaResult == SendCaptchaResult.Success
    val isLoading =
        sendCaptchaResult == SendCaptchaResult.Loading || loginResult == PhoneLoginResult.Loading

    LaunchedEffect(loginResult) {
        if (loginResult == PhoneLoginResult.Success) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(96.dp))

            Image(
                painter = painterResource(id = R.drawable.login_logo),
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(96.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = {
                    phoneNumber = it
                    viewModel.phoneNumberChanged(it)
                },
                label = { Text(stringResource(id = R.string.prompt_phone_number)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = if (isCaptchaSent) ImeAction.Next else ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (phoneNumberState?.isDataValid == true && !isCaptchaSent) {
                            viewModel.sendCaptcha(phoneNumber)
                        }
                    }
                ),
                isError = phoneNumberState?.error != null,
                supportingText = {
                    phoneNumberState?.error?.let {
                        Text(stringResource(id = it))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                singleLine = true
            )

            if (isCaptchaSent) {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = captcha,
                    onValueChange = {
                        captcha = it
                        viewModel.captchaChanged(phoneNumber, it)
                    },
                    label = { Text(stringResource(id = R.string.prompt_captcha)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (captchaState?.isDataValid == true) {
                                viewModel.login(phoneNumber, captcha)
                            }
                        }
                    ),
                    isError = captchaState?.error != null,
                    supportingText = {
                        captchaState?.error?.let {
                            Text(it)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (!isCaptchaSent) {
                        viewModel.sendCaptcha(phoneNumber)
                    } else {
                        viewModel.login(phoneNumber, captcha)
                    }
                },
                enabled = if (!isCaptchaSent) phoneNumberState?.isDataValid == true else captchaState?.isDataValid == true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
            ) {
                Text(
                    text = if (!isCaptchaSent) "发送验证码" else stringResource(id = R.string.action_sign_in)
                )
            }
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 200.dp)
            )
        }

        TextButton(
            onClick = onNavigateToPasswordLogin,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text("密码登录")
        }
    }
}