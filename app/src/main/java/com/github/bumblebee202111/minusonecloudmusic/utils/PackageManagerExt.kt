package com.github.bumblebee202111.minusonecloudmusic.utils

import android.content.pm.PackageManager
    fun PackageManager.isPackageInstalled(packageName: String): Boolean {
        return try {
            getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }