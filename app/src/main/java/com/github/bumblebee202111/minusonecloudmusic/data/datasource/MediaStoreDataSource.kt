package com.github.bumblebee202111.minusonecloudmusic.data.datasource

import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.github.bumblebee202111.minusonecloudmusic.model.LocalAlbum
import com.github.bumblebee202111.minusonecloudmusic.model.LocalSong
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreDataSource @Inject constructor(@ApplicationContext private val context: Context) {

    fun getMusicResources(): MutableList<LocalSong> {

        val contentResolver = context.contentResolver

        val songList = mutableListOf<LocalSong>()

        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

        val pathColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.MediaColumns.RELATIVE_PATH
        } else {
            MediaStore.Images.Media.DATA
        }
        val selection =
            "$pathColumn like ? "

        val selectionArgs = arrayOf("%netease/cloudmusic/Music%")

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
        )
        val query =
            contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                null
            )

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val nameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val durationColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val album = cursor.getString(albumColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val name = cursor.getString(nameColumn)
                val duration = cursor.getInt(durationColumn)
                val size = cursor.getInt(sizeColumn)
                songList += LocalSong(
                    name = title,
                    id = id,
                    album = LocalAlbum(name = album, art = null ),
                    artists = listOf(artist),
                    available = true
                )
            }
        }
        return songList
    }

    private companion object {
        val ncmDownloadPaths =
            arrayOf("Download/netease/cloudmusic/Music", "netease/cloudmusic/Music")
    }
}