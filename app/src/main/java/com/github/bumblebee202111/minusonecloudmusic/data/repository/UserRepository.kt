package com.github.bumblebee202111.minusonecloudmusic.data.repository

import androidx.room.withTransaction
import com.github.bumblebee202111.minusonecloudmusic.data.AppResult
import com.github.bumblebee202111.minusonecloudmusic.data.database.AppDatabase
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.UserDetailEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.UserPlaylistEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.toUserProfile
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.toPlaylist
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.populated.PopulatedMyRecentMusicData
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.populated.toUserDetail
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.populated.toMyRecentMusicData
import com.github.bumblebee202111.minusonecloudmusic.data.datastore.PreferenceStorage
import com.github.bumblebee202111.minusonecloudmusic.model.FollowedUserProfile
import com.github.bumblebee202111.minusonecloudmusic.model.UserDetail
import com.github.bumblebee202111.minusonecloudmusic.data.network.NcmEapiService
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.combine
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.MusicInfoApiModel
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.SongPrivilegeApiModel
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.toPlaylistEntity
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.toRemoteSongEntity
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.recentplay.MyRecentMusicDataApiModel
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.recentplay.toMyRecentMusicDataEntity
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.user.UserDetailApiModel
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.user.toUserProfileEntity
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.user.toFollowedUserProfile
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.user.toUserProfile
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val preferenceStorage: PreferenceStorage,
    private val appDatabase: AppDatabase,
    private val ncmEapiService: NcmEapiService,
    private val moshiAdapter: JsonAdapter<Any>
) {

    private val userDao = appDatabase.userDao()
    private val playlistDao = appDatabase.playlistDao()
    private val recentPlayDao = appDatabase.recentPlayDao()
    private val musicInfoDao = appDatabase.songDao()

    fun getUserDetail(uid: Long, useCache: Boolean): Flow<AppResult<UserDetail?>> =
        if (useCache) getOfflineFirstUserDetail(uid) else getApiUserDetail(uid)


    private fun getApiUserDetail(uid: Long) = apiResultFlow(
        fetch = {
            ncmEapiService.getV1UserDetail(uid)
        },
        mapSuccess = { result -> UserDetail(result.listenSongs, result.profile.toUserProfile()) })

    private fun getOfflineFirstUserDetail(uid: Long) = offlineFirstApiResultFlow<UserDetailApiModel, UserDetail?>(loadFromDb = {
        userDao.observeUserDetail(uid).map { it?.toUserDetail() }
    }, call = {
        ncmEapiService.getV1UserDetail(uid)
    }, saveSuccess = { data ->
        userDao.insertUserProfile(data.profile.toUserProfileEntity())
        userDao.insertUserDetail(UserDetailEntity(data.profile.userId, data.listenSongs))
    })

    fun getCachedUserDetail(uid: Long) = userDao.observeUserDetail(uid)

    fun getUserPlaylists(userId: Long) = offlineFirstApiResultFlow(
        loadFromDb = {
            userDao.observeUserPlaylists(userId)
                .map { it.map { populatedUserPlaylist -> populatedUserPlaylist.playlist.toPlaylist() } }
        },
        call = { ncmEapiService.getUserPlaylists(userId) },
        saveSuccess = { ncmPlaylistsResult ->
            val userPlaylists = ncmPlaylistsResult.playlist.mapIndexed { index, p ->
                UserPlaylistEntity(
                    userId, p.id, index
                )
            }
            val playlists = ncmPlaylistsResult.playlist.map { it.toPlaylistEntity() }

            appDatabase.withTransaction {
                userDao.deleteUserPlaylists(userId)
                userDao.insertUserPlaylists(userPlaylists)
                playlistDao.insertPlaylist(playlists)
            }
        })

    fun getRecentPlayMusic() = offlineFirstApiResultFlow(loadFromDb = {
        recentPlayDao.observeMyRecentPlayMusic()
            .map { it.map(PopulatedMyRecentMusicData::toMyRecentMusicData) }
    }, call = {
        ncmEapiService.getMyRecentMusic().combine {
            ncmEapiService.getSongEnhancePrivilege(moshiAdapter.toJson(this.list.map { item ->
                item.musicInfo.id
            }))
        }
    }, saveSuccess = { pair ->
        recentPlayDao.deleteAndInsertRecentPlayMusic(
            pair.first.list.map(
                MyRecentMusicDataApiModel::toMyRecentMusicDataEntity
            )
        )
        musicInfoDao.insertRemoteSongs(pair.first.list.zip(pair.second) { a, b ->
            Pair(a.data, b)
        }.map(Pair<MusicInfoApiModel, SongPrivilegeApiModel>::toRemoteSongEntity))
    })


    fun getUserFollows(
        userId: Long,
        offset: Int = 0,
        limit: Int = 20
    ): Flow<AppResult<List<FollowedUserProfile>>> =
        apiResultFlow(
            fetch = { ncmEapiService.getUserFollows(userId, offset, limit) },
            mapSuccess = { result ->
                return@apiResultFlow result.follow.map { it.toFollowedUserProfile() }
            })

    fun getUserFans(userId: Long, offset: Int = 0, limit: Int = 20) =
        apiResultFlow(
            fetch = { ncmEapiService.getUserFolloweds(userId, offset, limit) },
            mapSuccess = { result ->
                return@apiResultFlow result.followeds.map { it.toFollowedUserProfile() }
            })


    fun getCachedUserProfile(userId: Long) = appDatabase.userDao().observeUserProfile(userId).map {
        it?.toUserProfile()
    }
}