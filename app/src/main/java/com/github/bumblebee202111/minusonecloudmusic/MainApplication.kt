package com.github.bumblebee202111.minusonecloudmusic

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient

@HiltAndroidApp
class MainApplication : Application(), SingletonImageLoader.Factory {
    override fun newImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context).components {
            add(
                OkHttpNetworkFetcherFactory(
                    callFactory = {
                        OkHttpClient()
                    }
                )
            )
        }
            .build()
    }
}