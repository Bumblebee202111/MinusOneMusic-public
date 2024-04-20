package com.github.bumblebee202111.minusonecloudmusic.data.repository

import androidx.room.withTransaction
import com.github.bumblebee202111.minusonecloudmusic.data.database.AppDatabase
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.UserDetailEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.UserPlaylistEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.asExternalModel
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.populated.PopulatedMyRecentMusicData
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.populated.asExternalModel
import com.github.bumblebee202111.minusonecloudmusic.data.datastore.PreferenceStorage
import com.github.bumblebee202111.minusonecloudmusic.data.model.UserDetail
import com.github.bumblebee202111.minusonecloudmusic.data.network.NetworkDataSource
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.asEntity
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.recentplay.MyRecentMusicDataApiModel
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.recentplay.asEntity
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.user.asEntity
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.user.asExternalModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val preferenceStorage: PreferenceStorage,
    private val appDatabase: AppDatabase,
    private val networkDataSource: NetworkDataSource
) {

    private val userDao = appDatabase.userDao()
    private val playlistDao = appDatabase.playlistDao()
    private val recentPlayDao = appDatabase.recentPlayDao()
    private val musicInfoDao = appDatabase.songDao()

    fun getUserDetail(uid: Long, useCache: Boolean) =
        if (useCache) getOfflineFirstUserDetail(uid) else getApiUserDetail(uid)


    private fun getApiUserDetail(uid: Long) = apiResultFlow(fetch = {
        networkDataSource.getUserDetail(uid)
    },
        mapSuccess = { result -> UserDetail(result.listenSongs, result.profile.asExternalModel()) })

    private fun getOfflineFirstUserDetail(uid: Long) = offlineFirstApiResultFlow(
        loadFromDb = {
            userDao.observeUserDetail(uid).map { it?.asExternalModel() }
        },
        call = {
            networkDataSource.getUserDetail(uid)
        },
        saveSuccess = { data ->
            userDao.insertUserProfile(data.profile.asEntity())
            userDao.insertUserDetail(UserDetailEntity(data.profile.userId, data.listenSongs))
        })

    fun getCachedUserDetail(uid: Long)=userDao.observeUserDetail(uid)

    fun getUserPlaylists(userId: Long) = offlineFirstApiResultFlow(
        loadFromDb = {
            userDao.observeUserPlaylists(userId)
                .map { it.map { populatedUserPlaylist -> populatedUserPlaylist.playlist.asExternalModel() } }
        },
        call = { networkDataSource.getUserPlaylists(userId) },
        saveSuccess = { ncmPlaylistsResult ->
            val userPlaylists =
                ncmPlaylistsResult.playlist.mapIndexed { index, p ->
                    UserPlaylistEntity(
                        userId,
                        p.id,
                        index
                    )
                }
            val playlists = ncmPlaylistsResult.playlist.map { it.asEntity() }

            appDatabase.withTransaction {
                userDao.deleteUserPlaylists(userId)
                userDao.insertUserPlaylists(userPlaylists)
                playlistDao.insertPlaylist(playlists)
            }
        }
    )

    fun getRecentPlayMusic() = offlineFirstApiResultFlow(
        loadFromDb = {
            recentPlayDao.observeMyRecentPlayMusic()
                .map { it.map(PopulatedMyRecentMusicData::asExternalModel) }
        },
        call = { networkDataSource.getMyRecentMusic() },
        saveSuccess = { myRecentMusicWrapper ->
            recentPlayDao.deleteAndInsertRecentPlayMusic(
                myRecentMusicWrapper.list.map(
                    MyRecentMusicDataApiModel::asEntity
                )
            )
            musicInfoDao.insertRemoteSongs(myRecentMusicWrapper.list.map { it.musicInfo.asEntity() })
        }
    )


    fun getUserFollows(userId: Long, offset: Int=0, limit: Int=20) =
        apiResultFlow(fetch = { networkDataSource.getUserFollows(userId, offset, limit) },
            mapSuccess = { result ->
                return@apiResultFlow result.follow.map { it.asExternalModel() }
            })

    fun getUserFans(userId: Long, offset: Int=0, limit: Int=20) =
        apiResultFlow(fetch = { networkDataSource.getUserFolloweds(userId, offset, limit) },
            mapSuccess = { result ->
                return@apiResultFlow result.followeds.map { it.asExternalModel() }
            })


    fun getCachedUserProfile(userId: Long) = appDatabase.userDao().observeUserProfile(userId).map { it?.asExternalModel()
         }
}