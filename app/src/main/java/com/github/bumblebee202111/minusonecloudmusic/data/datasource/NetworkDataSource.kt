package com.github.bumblebee202111.minusonecloudmusic.data.datasource

import com.github.bumblebee202111.minusonecloudmusic.data.network.NCMAOkHttpClient
import com.github.bumblebee202111.minusonecloudmusic.data.network.NcmBapiService
import com.github.bumblebee202111.minusonecloudmusic.data.network.NcmEapiService
import com.github.bumblebee202111.minusonecloudmusic.data.network.NcmOkHttpClient
import com.github.bumblebee202111.minusonecloudmusic.data.network.NcmWeapiService
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.ApiResult
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.LikedSongApiResult
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.MusicAudioQuality
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.NetworkBillboardGroup
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.mymusic.MlogMyCollectByTimeApiModel
import okhttp3.OkHttpClient
import javax.inject.Inject



@OptIn(ExperimentalStdlibApi::class)
class NetworkDataSource @Inject constructor(
    @NCMAOkHttpClient val ncmaOkHttpClient: OkHttpClient,
    @NcmOkHttpClient val ncmHttpClient: OkHttpClient
) {


    @Suppress("DEPRECATION")
    private val ncmBapiService = NcmBapiService.create(ncmaOkHttpClient)


    private val eapiService = NcmEapiService.create(ncmHttpClient)

    private val weapiService = NcmWeapiService.create(ncmHttpClient)

    suspend fun registerAnonimous(username: String) = this.eapiService.registerAnonimous(username)

    suspend fun refreshLoginToken() = eapiService.refreshLoginToken()

    suspend fun getLoginStatus() = ncmBapiService.getLoginStatus()

    suspend fun sendSMSCaptcha(phone: String) = this.eapiService.sendMSMCaptcha(phone)

    suspend fun cellphoneLogin(
        cellphone: String, password: String? = null, captcha: String? = null
    ) = this.weapiService.cellphoneLogin(
        phone = cellphone,
        password = password,
        captcha = captcha,
    )

    suspend fun verifyCaptcha(
        phone: String, captcha: String
    ) = this.eapiService.verifyCaptcha(phone = phone, captcha = captcha)

    suspend fun getUserDetail(uid: Long) = this.eapiService.getV1UserDetail(uid)

    suspend fun logout() = this.eapiService.logout()

    suspend fun getHomeDiscoveryPage(cursor: String) =
        eapiService.getHomepageBlockPage(cursor = cursor)

    suspend fun getUserPlaylists(uid: Long) = this.eapiService.getUserPlaylists(uid)
    suspend fun getStarMusicIds(): ApiResult<LikedSongApiResult> =
        this.eapiService.getStarMusicIds()

    suspend fun getUserFollows(
        uid: Long, offset: Int, limit: Int, order: Boolean = false
    ) = this.eapiService.getUserFollows(uid, offset, limit, order)

    suspend fun getUserFolloweds(
        uid: Long, offset: Int, limit: Int
    ) = this.eapiService.getUserFolloweds(uid, offset, limit)

    suspend fun getPlaylistV4Detail(
        id: Long, n: Int = 1000, s: Int = 5
    ) = this.eapiService.getPlaylistDetail(id = id, n = n, s = s)

    suspend fun getPlaylistPrivilege(
        id: Long, n: Int = 1000
    ) = this.eapiService.getPlaylistPrivilege(id = id, n = n)

    suspend fun getV6PlaylistDetail(
        id: Long, trackUpdateTime: Long = 0, n: Int = 1000, s: Int = 5
    ) = this.eapiService.getMyPlaylistDetail(id, trackUpdateTime, n, s)

    suspend fun getToplistDetail(): ApiResult<List<NetworkBillboardGroup>> =
        eapiService.getTopListDetailsV2()

    suspend fun getSongDetails(cJsonString: String) = this.eapiService.getSongDetails(cJsonString)

    suspend fun getCloudSongs(
        limit: Int, offset: Int = 0
    ) = this.eapiService.getV1Cloud(limit, offset)

    suspend fun getSongEnhancePrivilege(ids: String) = this.eapiService.getSongEnhancePrivilege(ids)

    @Deprecated("Deprecated by NCM")
    suspend fun getSongUrl(
        ids: String, br: Int = MusicAudioQuality.EXHIGH.bitrate
    ) = this.eapiService.getSongUrl(ids, br)

    suspend fun getSongUrlsV1(
        ids: String, level: String = MusicAudioQuality.EXHIGH.level
    ) = this.eapiService.getSongUrlsV1(ids, level)

    suspend fun getSongLyrics(songId: Long) = this.eapiService.getSongLyrics(songId)

    suspend fun getSongLikeCount(songId: Long) = eapiService.getSongRedCount(songId)

    suspend fun likeSong(like: Boolean, songId: Long) = eapiService.likeSong(like, songId)

    suspend fun songDownloadUrl(id: Long) = eapiService.getSongEnhanceDownloadUrlV1(id = id)

    suspend fun getCommentInfoResourceList(songId: String) =
        eapiService.getCommentInfoResourceList(songId)

    suspend fun getRecommendSongs() = this.eapiService.getV3DiscoveryRecommendSongs()

    suspend fun getMyRecentMusic(limit: Int = 300) = this.eapiService.getMyRecentMusic(limit)

    suspend fun getAlbumSublist(
        limit: Int, offset: Int
    ) = this.eapiService.getAlbumSublist(limit, offset)

    suspend fun getMlogMyCollectByTime(
        limit: Int
    ): ApiResult<MlogMyCollectByTimeApiModel> = this.eapiService.getMlogMyCollectByTime(limit)


    suspend fun getResourceExposureConfigs(
        resourcePosition: String,
        resourceId: String,
        exposureRecords: List<Unit> = emptyList(),
        source: String
    ) = eapiService.getResourceExposureConfigs(
        resourcePosition,
        resourceId,
        exposureRecords,
        source
    )

    suspend fun searchComplex(
        keyword: String,
        cursor: String,
    ) = eapiService.searchComplex(keyword = keyword, cursor = cursor)

    suspend fun getV2ResourceComments(
        threadId: String
    ) = eapiService.getV2ResourceComments(threadId = threadId)

    suspend fun getAllCloudVideoSublist(
        limit: Int, offset: Int, total: Boolean = true
    ) = ncmBapiService.getAllCloudVideoSublist(limit, offset, total)

    @Deprecated("Not standard NCM mobile API")
    suspend fun getLoginQrCodeKey() = ncmBapiService.getLoginQrCodeKey()

    @Deprecated("Not standard NCM mobile API")
    suspend fun getLoginQrCode(
        key: String, qrimg: Boolean = true
    ) = ncmBapiService.getLoginQrCode(key, qrimg)

    @Deprecated("Not standard NCM mobile API")
    suspend fun checkLoginQrCode(
        key: String, noCookie: Boolean? = null
    ) = ncmBapiService.checkLoginQrCode(key, noCookie)
}
