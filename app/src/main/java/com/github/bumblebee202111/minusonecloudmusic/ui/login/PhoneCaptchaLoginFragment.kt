package com.github.bumblebee202111.minusonecloudmusic.ui.login

import android.content.Context
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentPhoneCaptchaLoginBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PhoneCaptchaLoginFragment : Fragment() {

    private val phoneCaptchaLoginViewModel: PhoneCaptchaLoginViewModel by viewModels()
    private lateinit var binding: FragmentPhoneCaptchaLoginBinding

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
                        dismissKeyboard(phoneNumberEditText.windowToken)
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
                        dismissKeyboard(captchaEditText.windowToken)
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
                            showLoginFailed(loginResult.errorMsgResId)
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
            findNavController().navigate(R.id.nav_qrcode_login)
        }

    }

    private fun updateUiWithUser() {
        val welcome = getString(R.string.welcome)
        findNavController().popBackStack(R.id.nav_login, false)
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

    private fun showLoginFailed(errorString: String) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }


    private fun dismissKeyboard(windowToken: IBinder) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(windowToken, 0)
    }
}