package com.github.bumblebee202111.minusonecloudmusic.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentPhoneCaptchaLoginBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.hideSoftInput
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.NavigationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PhoneCaptchaLoginFragment : Fragment() {

    private val phoneCaptchaLoginViewModel: PhoneCaptchaLoginViewModel by viewModels()
    private lateinit var binding: FragmentPhoneCaptchaLoginBinding

    @Inject
    lateinit var navigationManager: NavigationManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentPhoneCaptchaLoginBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val phoneNumberEditText = binding.phoneNumber
        val captchaEditText = binding.captcha
        val loginButton = binding.login
        val loadingProgressBar = binding.loading

        repeatWithViewLifecycle {
            launch {
                phoneCaptchaLoginViewModel.phoneNumberState.collect { phoneNumberState ->
                    if (phoneNumberState == null) {
                        return@collect
                    }
                    loginButton.isEnabled = phoneNumberState.isDataValid
                    if (phoneNumberState.isDataValid) {
                        phoneNumberEditText.hideSoftInput()
                    }
                    phoneNumberState.error?.let {
                        phoneNumberEditText.error = getString(it)
                    }
                }
            }
            launch {
                phoneCaptchaLoginViewModel.captchaState.collect { captchaState ->
                    if (captchaState == null) {
                        return@collect
                    }
                    if (captchaState.isDataValid) {
                        captchaEditText.hideSoftInput()
                        phoneCaptchaLoginViewModel.login(
                            phoneNumberEditText.text.toString(),
                            captchaEditText.text.toString()
                        )
                    }
                    captchaState.error?.let {
                        captchaEditText.error = it
                    }
                }
            }

            launch {
                phoneCaptchaLoginViewModel.sendCaptchaResult.collect { sendCaptchaResult ->
                    when (sendCaptchaResult) {
                        is SendCaptchaResult.Loading -> {
                            loadingProgressBar.isVisible = true
                            loginButton.isVisible = false
                            captchaEditText.isVisible = false
                        }

                        is SendCaptchaResult.Success -> {
                            loadingProgressBar.isVisible = false
                            loginButton.isVisible = false
                            captchaEditText.isVisible = true
                        }

                        is SendCaptchaResult.Error -> {
                            loadingProgressBar.isVisible = false
                            loginButton.isVisible = true
                            captchaEditText.isVisible = false

                        }

                        else -> {

                        }
                    }
                }
            }

            launch {
                phoneCaptchaLoginViewModel.phoneLoginResult.collect { loginResult ->
                    when (loginResult) {
                        null -> return@collect
                        is PhoneLoginResult.Loading -> {
                            loadingProgressBar.isVisible = true
                        }

                        is PhoneLoginResult.Error -> {
                            loadingProgressBar.isVisible = false
                        }

                        is PhoneLoginResult.Success -> {
                            loadingProgressBar.isVisible = false
                            updateUiWithUser()
                        }
                    }
                }
            }
        }




        phoneNumberEditText.doAfterTextChanged {
            phoneCaptchaLoginViewModel.phoneNumberChanged(
                it.toString(),
            )
        }
        captchaEditText.doAfterTextChanged {
            phoneCaptchaLoginViewModel.captchaChanged(
                phoneNumberEditText.text.toString(),
                it.toString()
            )
        }
        captchaEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                phoneCaptchaLoginViewModel.login(
                    phoneNumberEditText.text.toString(),
                    captchaEditText.text.toString()
                )
            }
            false
        }

        loginButton.setOnClickListener {
            phoneCaptchaLoginViewModel.sendCaptcha(phoneNumberEditText.text.toString())
        }

        binding.moreLoginModes.setOnClickListener {

        }

    }

    private fun updateUiWithUser() {
        navigationManager.goBack()
    }

}