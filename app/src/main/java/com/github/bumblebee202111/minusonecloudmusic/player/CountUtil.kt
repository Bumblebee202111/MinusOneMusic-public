package com.github.bumblebee202111.minusonecloudmusic.player

object CountUtil {

    fun getAbbreviatedCommentCount(count: Int): String {
        if (count <= 999) {
            return count.toString()
        }
        if (count <= 9999) {
            return "999+"
        }
        if (count <= 99999) {
            return "1w+"
        }
        return if (count <= 999999) {
            "10w+"
        } else "100w+"
    }

}