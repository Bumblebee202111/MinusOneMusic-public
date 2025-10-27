package com.github.bumblebee202111.minusonecloudmusic.data.database

import android.net.Uri
import androidx.core.net.toUri
import androidx.room.TypeConverter
import com.github.bumblebee202111.minusonecloudmusic.model.LocalAlbum
import com.github.bumblebee202111.minusonecloudmusic.model.RemoteAlbum
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object TypeConverters {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val remoteAlbumAdapter: JsonAdapter<RemoteAlbum> =
        moshi.adapter(RemoteAlbum::class.java)
    private val localAlbumAdapter: JsonAdapter<LocalAlbum> = moshi.adapter(LocalAlbum::class.java)
    private val stringListAdapter: JsonAdapter<List<String?>> = moshi.adapter(stringListType)

    @TypeConverter
    fun remoteAlbumToString(album: RemoteAlbum): String {
        return remoteAlbumAdapter.toJson(album)
    }

    @TypeConverter
    fun stringToRemoteAlbum(string: String): RemoteAlbum? {
        return remoteAlbumAdapter.fromJson(string)
    }

    @TypeConverter
    fun localAlbumToString(album: LocalAlbum): String {
        return localAlbumAdapter.toJson(album)
    }

    @TypeConverter
    fun stringToLocalAlbum(string: String): LocalAlbum? {
        return localAlbumAdapter.fromJson(string)
    }

    @TypeConverter
    fun stringListToString(stringList: List<String?>): String {
        return stringListAdapter.toJson(stringList)
    }

    @TypeConverter
    fun stringToStringList(string: String): List<String?>? {
        return stringListAdapter.fromJson(string)
    }

    @TypeConverter
    fun uriToString(uri: Uri): String {
        return uri.toString()
    }

    @TypeConverter
    fun stringToUri(string: String): Uri {
        return string.toUri()
    }

}