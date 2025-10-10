package com.github.bumblebee202111.minusonecloudmusic.data.repository

import androidx.room.withTransaction
import com.github.bumblebee202111.minusonecloudmusic.data.AppResult
import com.github.bumblebee202111.minusonecloudmusic.data.database.AppDatabase
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.UserDetailEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.UserPlaylistEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.asExternalModel
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.populated.PopulatedMyRecentMusicData
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.populated.asExternalModel
import com.github.bumblebee202111.minusonecloudmusic.data.datastore.PreferenceStorage
import com.github.bumblebee202111.minusonecloudmusic.data.model.FollowedUserProfile
import com.github.bumblebee202111.minusonecloudmusic.data.model.UserDetail
import com.github.bumblebee202111.minusonecloudmusic.data.network.NcmEapiService
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.combine
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.MusicInfoApiModel
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.SongPrivilegeApiModel
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.asEntity
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.recentplay.MyRecentMusicDataApiModel
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.recentplay.asEntity
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.user.UserDetailApiModel
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.user.asEntity
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.user.asExternalModel
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
        mapSuccess = { result -> UserDetail(result.listenSongs, result.profile.asExternalModel()) })

    private fun getOfflineFirstUserDetail(uid: Long) = offlineFirstApiResultFlow<UserDetailApiModel, UserDetail?>(loadFromDb = {
        userDao.observeUserDetail(uid).map { it?.asExternalModel() }
    }, call = {
        ncmEapiService.getV1UserDetail(uid)
    }, saveSuccess = { data ->
        userDao.insertUserProfile(data.profile.asEntity())
        userDao.insertUserDetail(UserDetailEntity(data.profile.userId, data.listenSongs))
    })

    fun getCachedUserDetail(uid: Long) = userDao.observeUserDetail(uid)

    fun getUserPlaylists(userId: Long) = offlineFirstApiResultFlow(
        loadFromDb = {
            userDao.observeUserPlaylists(userId)
                .map { it.map { populatedUserPlaylist -> populatedUserPlaylist.playlist.asExternalModel() } }
        },
        call = { ncmEapiService.getUserPlaylists(userId) },
        saveSuccess = { ncmPlaylistsResult ->
            val userPlaylists = ncmPlaylistsResult.playlist.mapIndexed { index, p ->
                UserPlaylistEntity(
                    userId, p.id, index
                )
            }
            val playlists = ncmPlaylistsResult.playlist.map { it.asEntity() }

            appDatabase.withTransaction {
                userDao.deleteUserPlaylists(userId)
                userDao.insertUserPlaylists(userPlaylists)
                playlistDao.insertPlaylist(playlists)
            }
        })

    fun getRecentPlayMusic() = offlineFirstApiResultFlow(loadFromDb = {
        recentPlayDao.observeMyRecentPlayMusic()
            .map { it.map(PopulatedMyRecentMusicData::asExternalModel) }
    }, call = {
        ncmEapiService.getMyRecentMusic().combine {
            ncmEapiService.getSongEnhancePrivilege(moshiAdapter.toJson(this.list.map { item ->
                item.musicInfo.id
            }))
        }
    }, saveSuccess = { pair ->
        recentPlayDao.deleteAndInsertRecentPlayMusic(
            pair.first.list.map(
                MyRecentMusicDataApiModel::asEntity
            )
        )
        musicInfoDao.insertRemoteSongs(pair.first.list.zip(pair.second) { a, b ->
            Pair(a.data, b)
        }.map(Pair<MusicInfoApiModel, SongPrivilegeApiModel>::asEntity))
    })


    fun getUserFollows(
        userId: Long,
        offset: Int = 0,
        limit: Int = 20
    ): Flow<AppResult<List<FollowedUserProfile>>> =
        apiResultFlow(
            fetch = { ncmEapiService.getUserFollows(userId, offset, limit) },
            mapSuccess = { result ->
                return@apiResultFlow result.follow.map { it.asExternalModel() }
            })

    fun getUserFans(userId: Long, offset: Int = 0, limit: Int = 20) =
        apiResultFlow(
            fetch = { ncmEapiService.getUserFolloweds(userId, offset, limit) },
            mapSuccess = { result ->
                return@apiResultFlow result.followeds.map { it.asExternalModel() }
            })


    fun getCachedUserProfile(userId: Long) = appDatabase.userDao().observeUserProfile(userId).map {
        it?.asExternalModel()
    }
}