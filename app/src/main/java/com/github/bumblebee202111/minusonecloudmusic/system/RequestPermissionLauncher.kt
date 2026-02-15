package com.github.bumblebee202111.minusonecloudmusic.system

import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment


fun Fragment.requestPermissionLauncher(onPermissionGranted: () -> Unit): ActivityResultLauncher<String> =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) onPermissionGranted()
    }

context (fragment: Fragment)
fun ActivityResultLauncher<String>.launchRequestPermission(
    permission: String,
    onPermissionGranted: () -> Unit
) {
    if (ActivityCompat.checkSelfPermission(
            fragment.requireContext(),
            permission
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        launch(permission)
    } else {
        onPermissionGranted()
    }
}
