package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.github.bumblebee202111.minusonecloudmusic.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
val Fragment.statusBarHeight:Int
    @SuppressLint("DiscouragedApi", "InternalInsetResource")
    get() {
        var result = 0
        val resourceId: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
val Fragment.mainNavController:NavController
    get() = requireActivity().findNavController(R.id.nav_host_fragment_content_main)

inline fun Fragment.repeatWithViewLifecycle(
    minState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    if (minState == Lifecycle.State.INITIALIZED || minState == Lifecycle.State.DESTROYED) {
        throw IllegalArgumentException("minState must be between INITIALIZED and DESTROYED")
    }
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.lifecycle.repeatOnLifecycle(minState) {
            block()
        }
    }
}

fun Fragment.requireFragment(id: Int): Fragment {
    return childFragmentManager.findFragmentById(id) ?: throw IllegalArgumentException()
}
