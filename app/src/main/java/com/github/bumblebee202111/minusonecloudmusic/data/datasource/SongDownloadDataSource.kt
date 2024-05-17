package com.github.bumblebee202111.minusonecloudmusic.data.datasource

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.core.net.toUri
import com.github.bumblebee202111.minusonecloudmusic.data.model.RemoteSong
import com.github.bumblebee202111.minusonecloudmusic.data.model.displayArtists
import com.github.bumblebee202111.minusonecloudmusic.data.model.displayName
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@SuppressLint("UnsafeOptInUsageError")
@Singleton
class SongDownloadDataSource @Inject constructor(@ApplicationContext private val context: Context) {

    private val downloadManager: DownloadManager =
        context.getSystemService(DownloadManager::class.java)

    fun download(song: RemoteSong, url: String): Long {
        val filename = "${song.displayArtists} - ${song.displayName}.${url.substringAfterLast('.')}"
        val request = DownloadManager.Request(url.toUri())
            .setTitle(song.displayName)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "netease/cloudmusic/Music/$filename"
            )
        return downloadManager.enqueue(request)
    }
}