package com.github.bumblebee202111.minusonecloudmusic.ui.mine

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.github.bumblebee202111.minusonecloudmusic.R


data class MineDragonBall(
    val id: String,
    val miniProgramId: String?,
    val code: String,
    val iconUrl: String?,
    @DrawableRes
    val iconResId: Int,
    @StringRes
    val nameResId: Int,
    val orpheus: String,
    val redPoint: Boolean,
    val isPinned: Boolean,
    val ext: String?,
    val extraMap: Map<String, Any?>?,
    val lottieUrl: String?,
    val layers: List<Any?>?,
    val traceId: String?,
    val fromCache: Boolean
) {

    constructor(
        id: String,
        miniProgramId: String?,
        code: String,
        iconUrl: String?,
        iconResId: Int,
        nameResId: Int,
        orpheus: String,
        redPoint: Boolean,
        isPinned: Boolean,
        ext: String?,
        extraMap: Map<String, Any?>?,
        lottieUrl: String?,
        layers: List<*>?,
        traceId: String?,
        fromCache: Boolean,
        i: Int
    ) :
            this(
                id = id,
                miniProgramId = miniProgramId,
                code = code,
                iconUrl = iconUrl,
                iconResId = iconResId,
                nameResId = nameResId,
                orpheus = orpheus,
                redPoint = redPoint,
                isPinned = isPinned,
                ext = ext,
                extraMap = if (i and 1024 != 0) java.util.HashMap() else extraMap,
                lottieUrl = if (i and 2048 != 0) null else lottieUrl,
                layers = if (i and 4096 != 0) arrayListOf() else layers,
                traceId = if (i and 8192 != 0) null else traceId,
                fromCache = if (i and 16384 != 0) false else fromCache
            )


    companion object {

        private val CLOUD_DISK: MineDragonBall
        private val CODE_TO_ID: Map<String, String>
        private val COLLECTION: MineDragonBall
        private val FOLLOW: MineDragonBall
        private val LOCAL_MUSIC: MineDragonBall
        private val RECENT_PLAY: MineDragonBall
        const val TYPE_ADD_MINI_PROGRAM = "MUSIC_APPLET"
        const val TYPE_CLOUD_DISK = "PRIVATE_CLOUD"
        const val TYPE_COLLECTION = "SUBSCRIBE_PRAISE"
        const val TYPE_FOLLOW = "FOLLOW"
        const val TYPE_LOCAL_MUSIC = "LOCAL_MUSIC"
        const val TYPE_RECENT_PLAY = "RECENT_PLAY"
        val PINNED_DRAGON_BALLS: List<MineDragonBall>

        init {
            CODE_TO_ID = mapOf(
                Pair(TYPE_LOCAL_MUSIC, "32000"),
                Pair(TYPE_CLOUD_DISK, "33000"),
                Pair("RECENT_PLAY", "35000"),
                Pair("FOLLOW", "36000"),
                Pair(TYPE_COLLECTION, "34001"),
                Pair(
                    TYPE_ADD_MINI_PROGRAM,
                    TYPE_ADD_MINI_PROGRAM
                )
            )

            LOCAL_MUSIC = MineDragonBall(
                id = "32000",
                miniProgramId = null,
                code = TYPE_LOCAL_MUSIC,
                iconUrl = "http:
                iconResId = R.drawable.ic_my_music_dragon_ball_local_music,
                nameResId = R.string.playSourceLocal,
                orpheus = "orpheus:
                redPoint = false,
                isPinned = true,
                ext = null,
                extraMap = null,
                lottieUrl = null,
                layers = null,
                traceId = null,
                fromCache = false,
                i = 31744
            )

            CLOUD_DISK = MineDragonBall(
                id = "33000",
                miniProgramId = null,
                code = TYPE_CLOUD_DISK,
                iconUrl = "http:
                iconResId =R.drawable.ic_my_music_dragon_ball_cloud_disk,
                nameResId = R.string.my_music_dragon_ball_cloud_disk,
                orpheus = "orpheus:
                redPoint = false,
                isPinned = true,
                ext = null,
                extraMap = null,
                lottieUrl = null,
                layers = null,
                traceId = null,
                fromCache = false,
                i = 31744,
            )

            RECENT_PLAY = MineDragonBall(
                id = "35000",
                miniProgramId = null,
                code = "RECENT_PLAY",
                iconUrl = "http:
                iconResId =R.drawable.ic_my_music_dragon_ball_recent_play,
                nameResId =  R.string.my_music_dragon_ball_recent_play,
                orpheus = "orpheus:
                redPoint = false,
                isPinned = true,
                ext = null,
                extraMap = null,
                lottieUrl = null,
                layers = null,
                traceId = null,
                fromCache = false,
                i = 31744,
            )

            FOLLOW = MineDragonBall(
                id = "36000",
                miniProgramId = null,
                code = "FOLLOW",
                iconUrl = "http:
                iconResId = R.drawable.ic_my_music_dragon_ball_follow,
                nameResId = R.string.my_music_dragon_ball_follow,
                orpheus = "orpheus:
                redPoint = false,
                isPinned = true,
                ext = null,
                extraMap = null,
                lottieUrl = null,
                layers = null,
                traceId = null,
                fromCache = false,
                i = 31744,
            )

            COLLECTION = MineDragonBall(
                id = "34001",
                miniProgramId = null,
                code = TYPE_COLLECTION,
                iconUrl = "http:
                iconResId = R.drawable.ic_my_music_dragon_ball_collection,
                nameResId = R.string.my_music_dragon_ball_collection,
                orpheus = "orpheus:
                redPoint = false,
                isPinned = true,
                ext = null,
                extraMap = null,
                lottieUrl = null,
                layers = null,
                traceId = null,
                fromCache = false,
                i = 31744,
            )
            PINNED_DRAGON_BALLS = listOf(
                LOCAL_MUSIC,
                CLOUD_DISK,
                RECENT_PLAY,
                FOLLOW,
                COLLECTION
            )

        }
    }
}

