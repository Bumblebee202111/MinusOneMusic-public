package com.github.bumblebee202111.minusonecloudmusic.data.database.model.view

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import com.github.bumblebee202111.minusonecloudmusic.data.model.LocalAlbum
import com.github.bumblebee202111.minusonecloudmusic.data.model.LocalSong
import com.github.bumblebee202111.minusonecloudmusic.data.model.RemoteAlbum
import com.github.bumblebee202111.minusonecloudmusic.data.model.RemoteSong

@DatabaseView(
    value = """
SELECT 0 AS is_local, id, name, album, artists, available, is_downloadable, version FROM RemoteSongEntity
UNION ALL
SELECT 1 AS is_local, id, name, album, artists, available, NULL AS is_downloadable, NULL AS version FROM LocalSongEntity
""",
    viewName = "generic_songs"
)
data class GenericSongView(
    @ColumnInfo("is_local")
    val isLocal: Boolean,
    val name: String?,
    val id: Long,
    val album: Album?,
    val artists: List<String?>,
    val available: Boolean,
    @ColumnInfo("is_downloadable")
    val isDownloadable: Boolean,
    val version: Int?,
) {
    data class Album(
        val id: Long?,
        val name: String?,
        val pictureUrl: String?,
        var art: ByteArray?
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Album

            if (id != other.id) return false
            if (name != other.name) return false
            if (pictureUrl != other.pictureUrl) return false
            if (art != null) {
                if (other.art == null) return false
                if (!art.contentEquals(other.art)) return false
            } else if (other.art != null) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + (name?.hashCode() ?: 0)
            result = 31 * result + (pictureUrl?.hashCode() ?: 0)
            result = 31 * result + (art?.contentHashCode() ?: 0)
            return result
        }
    }
}

fun GenericSongView.asExternalModel() = if (isLocal)
    LocalSong(
        name = name,
        id = id,
        album = album?.let { LocalAlbum(it.name, it.art) },
        artists = artists,
        available = available
    )
else
    RemoteSong(
        name = name,
        id = id,
        album = album?.let { RemoteAlbum(it.id!!, it.name, it.pictureUrl!!) },
        artists = artists,
        available = available,
        isDownloadable = isDownloadable,
        version = version!!
    )
