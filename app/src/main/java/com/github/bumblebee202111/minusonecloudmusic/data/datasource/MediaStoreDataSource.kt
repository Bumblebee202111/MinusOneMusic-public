package com.github.bumblebee202111.minusonecloudmusic.data.datasource

import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.github.bumblebee202111.minusonecloudmusic.coroutines.AppDispatchers.IO
import com.github.bumblebee202111.minusonecloudmusic.coroutines.Dispatcher
import com.github.bumblebee202111.minusonecloudmusic.model.LocalAlbum
import com.github.bumblebee202111.minusonecloudmusic.model.LocalSong
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun getMusicResources(): List<LocalSong> = withContext(ioDispatcher) {

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
            @Suppress("DEPRECATION")
            MediaStore.MediaColumns.DATA
        }
        val selection =
            "$pathColumn like ? "

        val selectionArgs = arrayOf("%netease/cloudmusic/Music%")

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
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

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val album = cursor.getString(albumColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)

                songList += LocalSong(
                    name = title,
                    id = id,
                    album = LocalAlbum(name = album, art = null ),
                    artists = listOf(artist),
                    available = true
                )
            }
        }
        songList
    }
}