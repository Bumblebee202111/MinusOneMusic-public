package com.github.bumblebee202111.minusonecloudmusic.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationManager @Inject constructor() {
    private val _navActions = MutableSharedFlow<NavKey>(extraBufferCapacity = 1)
    val navActions = _navActions.asSharedFlow()

    private val _backActions = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val backActions = _backActions.asSharedFlow()

    fun navigate(route: NavKey) {
        _navActions.tryEmit(route)
    }

    fun goBack() {
        _backActions.tryEmit(Unit)
    }
}