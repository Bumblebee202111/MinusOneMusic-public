package com.github.bumblebee202111.minusonecloudmusic.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentSettingsBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var settingsViewModel: SettingsViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        settingsViewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val textView: TextView = binding.textSlideshow

        repeatWithViewLifecycle {
            launch {
                settingsViewModel.text.collect {
                    textView.text = it
                }
            }
        }
    }
}
