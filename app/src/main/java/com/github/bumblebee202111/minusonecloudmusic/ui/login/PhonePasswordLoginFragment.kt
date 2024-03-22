package com.github.bumblebee202111.minusonecloudmusic.ui.login

import android.content.Context
import android.os.Bundle
import android.os.IBinder
import android.text.Editable
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
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentPhonePasswordLoginBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PhonePasswordLoginFragment : Fragment() {

    private val viewModel: PhonePasswordLoginViewModel by viewModels()
    private lateinit var binding: FragmentPhonePasswordLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentPhonePasswordLoginBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val phoneNumberEditText = binding.phoneNumber
        val passwordEditText = binding.password
        val loginButton = binding.login
        val loadingProgressBar = binding.loading


        repeatWithViewLifecycle {
            launch{        viewModel.loginFormState.collect { loginFormState ->
                if (loginFormState == null) {
                    return@collect
                }
                loginButton.isEnabled = loginFormState.isDataValid

                loginFormState.phoneNumberError?.let {
                    phoneNumberEditText.error = getString(it)
                }
                loginFormState.passwordError?.let {
                    passwordEditText.error = getString(it)
                }
            }}

            launch{        viewModel.phoneLoginResult.collect { loginResult ->
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
            }}
        }


        val doAfterTextChanged = { _: Editable? ->
            viewModel.loginDataChanged(
                phoneNumberEditText.text.toString(), passwordEditText.text.toString()
            )
        }
        phoneNumberEditText.doAfterTextChanged(action = doAfterTextChanged)
        passwordEditText.doAfterTextChanged (   action =   doAfterTextChanged   )
        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.login(
                    phoneNumberEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
            false
        }

        loginButton.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE
            viewModel.login(phoneNumberEditText.text.toString(), passwordEditText.text.toString())
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