package com.github.bumblebee202111.minusonecloudmusic.ui.login

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentPhonePasswordLoginBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.NavigationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PhonePasswordLoginFragment : Fragment() {

    private val viewModel: PhonePasswordLoginViewModel by viewModels()
    private lateinit var binding: FragmentPhonePasswordLoginBinding

    @Inject
    lateinit var navigationManager: NavigationManager

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

        }

    }

    private fun updateUiWithUser() {
        navigationManager.goBack()
    }

}