package com.github.bumblebee202111.minusonecloudmusic.player

import androidx.annotation.OptIn
import androidx.media3.common.Metadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.extractor.metadata.id3.CommentFrame
import androidx.media3.extractor.metadata.id3.TextInformationFrame
import androidx.media3.extractor.metadata.vorbis.VorbisComment
import com.github.bumblebee202111.minusonecloudmusic.model.LyricsEntry


@OptIn(UnstableApi::class)
fun Metadata?.extractEmbeddedLyrics(): List<LyricsEntry>? {
    if (this == null) return null
    var rawLyrics: String? = null

    for (i in 0 until length()) {
        val entry = get(i)
        if (entry is CommentFrame && (entry.id == "USLT" || entry.description.equals("LYRICS", true))) {
            rawLyrics = entry.text
            break
        }
        if (entry is TextInformationFrame && entry.id == "TXXX" && entry.description?.equals("LYRICS", true) == true) {
            rawLyrics = entry.values.first()
            break
        }
        if (entry is VorbisComment && (entry.key.equals("LYRICS", true) || entry.key.equals("UNSYNCEDLYRICS", true))) {
            rawLyrics = entry.value
            break
        }
    }

    return LyricsEntry.parseLrc(rawLyrics).takeIf { it.isNotEmpty() }
}