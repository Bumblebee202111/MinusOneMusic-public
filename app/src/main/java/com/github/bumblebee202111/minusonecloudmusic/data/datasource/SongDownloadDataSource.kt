package com.github.bumblebee202111.minusonecloudmusic.data.datasource

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.core.net.toUri
import com.github.bumblebee202111.minusonecloudmusic.model.RemoteSong
import com.github.bumblebee202111.minusonecloudmusic.model.SongDownloadInfo
import com.github.bumblebee202111.minusonecloudmusic.model.artistsAsPartOfFilename
import com.github.bumblebee202111.minusonecloudmusic.model.nameAsPartOfFilename
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@SuppressLint("UnsafeOptInUsageError")
@Singleton
class SongDownloadDataSource @Inject constructor(@ApplicationContext private val context: Context) {

    private val downloadManager: DownloadManager =
        context.getSystemService(DownloadManager::class.java)

    fun download(song: RemoteSong, songDownloadInfo: SongDownloadInfo): Long {
        val filename =
            "${song.artistsAsPartOfFilename} - ${song.nameAsPartOfFilename}.${songDownloadInfo.extension}"
        val request = DownloadManager.Request(songDownloadInfo.url.toUri())
            .setTitle(song.nameAsPartOfFilename)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "netease/cloudmusic/Music/$filename"
            )
        return downloadManager.enqueue(request)
    }
}