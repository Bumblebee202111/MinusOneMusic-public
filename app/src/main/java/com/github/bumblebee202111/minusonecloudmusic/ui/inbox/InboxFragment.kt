package com.github.bumblebee202111.minusonecloudmusic.ui.inbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentInboxBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InboxFragment : Fragment() {

    private lateinit var binding: FragmentInboxBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inboxViewModel =
            ViewModelProvider(this)[InboxViewModel::class.java]

        binding = FragmentInboxBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textInbox

        repeatWithViewLifecycle {
            launch {
                inboxViewModel.text.collect {
                    textView.text = it
                }

            }
        }
        return root
    }


}