package com.github.bumblebee202111.minusonecloudmusic.data.database

import androidx.room.TypeConverter
import com.github.bumblebee202111.minusonecloudmusic.data.model.Album
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object TypeConverters {

    private val moshi= Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    private val stringListType= Types.newParameterizedType(List::class.java,String::class.java)
    private val albumAdapter:JsonAdapter<Album> =moshi.adapter(Album::class.java)
    private val stringListAdapter:JsonAdapter<List<String?>> =moshi.adapter(stringListType)
    @TypeConverter
    fun albumToString(album: Album): String {
        return albumAdapter.toJson(album)
    }
    @TypeConverter
    fun stringToAlbum(string: String): Album? {
        return albumAdapter.fromJson(string)
    }
    @TypeConverter
    fun stringListToString(stringList: List<String?>): String {
        return stringListAdapter.toJson(stringList)
    }
    @TypeConverter
    fun stringToStringList(string: String): List<String?>? {
        return stringListAdapter.fromJson(string)
    }

}