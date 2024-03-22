package com.github.bumblebee202111.minusonecloudmusic.ui.login

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.fragment.findNavController
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.login.LoginQrCodeStatusCodes
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentQrCodeLoginBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QrCodeLoginFragment : Fragment() {
    companion object {
        const val LOGIN_SUCCESSFUL: String = "LOGIN_SUCCESSFUL"
    }

    private val qrCodeLoginViewModel: QrCodeLoginViewModel by viewModels()
    private lateinit var binding: FragmentQrCodeLoginBinding
    private lateinit var savedStateHandle: SavedStateHandle

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQrCodeLoginBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedStateHandle = findNavController().previousBackStackEntry!!.savedStateHandle
        savedStateHandle[LOGIN_SUCCESSFUL] = false

        val loadingProgressBar = binding.loading

        binding.moreLoginModes.setOnClickListener { v ->
        }

    repeatWithViewLifecycle {
        launch {
            qrCodeLoginViewModel.loginQrCodeStatus.collect { status ->
                when (status?.code) {
                    LoginQrCodeStatusCodes.EXPIRED -> {
                        loadingProgressBar.visibility = View.GONE
                        showLoginFailed()
                    }

                    LoginQrCodeStatusCodes.CONFIRMING -> {
                        loadingProgressBar.visibility = View.VISIBLE
                    }

                    LoginQrCodeStatusCodes.SUCCESS -> {
                        loadingProgressBar.visibility = View.GONE
                        savedStateHandle[LOGIN_SUCCESSFUL] = true
                        updateUiWithUser()
                    }

                    else -> {

                    }
                }
            }
        }
        launch {
            qrCodeLoginViewModel.loginQrCode.collect {
                it?.let { qrCodeString ->
                    val decodedString: ByteArray =
                        Base64.decode(qrCodeString.substringAfter(','), Base64.DEFAULT)
                    val decodedByte =
                        BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                    binding.qrCode.setImageBitmap(decodedByte)
                }
            }



        }
    }
    }



    private fun updateUiWithUser() {
        val welcome = getString(R.string.welcome)
        findNavController().popBackStack(R.id.nav_login,false)

        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, welcome,Toast.LENGTH_LONG).show()
    }

    private fun showLoginFailed() {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, R.string.login_failed, Toast.LENGTH_LONG).show()
    }


}