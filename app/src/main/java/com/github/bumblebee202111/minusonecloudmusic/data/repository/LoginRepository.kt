package com.github.bumblebee202111.minusonecloudmusic.data.repository

import com.github.bumblebee202111.minusonecloudmusic.data.Result
import com.github.bumblebee202111.minusonecloudmusic.data.database.AppDatabase
import com.github.bumblebee202111.minusonecloudmusic.data.datastore.PreferenceStorage
import com.github.bumblebee202111.minusonecloudmusic.data.network.NcmEapiService
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.user.asEntity
import com.github.bumblebee202111.minusonecloudmusic.data.network.util.encodedAsBase64String
import com.github.bumblebee202111.minusonecloudmusic.data.network.util.md5
import com.github.bumblebee202111.minusonecloudmusic.utils.AppAndDeviceInfoProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.experimental.xor

@OptIn(ExperimentalStdlibApi::class)
@Singleton
class LoginRepository @Inject constructor(
    private val preferenceStorage: PreferenceStorage,
    private val appDatabase: AppDatabase,
    private val ncmEapiService: NcmEapiService,
    private val appAndDeviceInfoProvider: AppAndDeviceInfoProvider
) {
    private val userDao = appDatabase.userDao()

    fun registerAnonymous(): Flow<Result<Unit?>> {
        val deviceId =
            appAndDeviceInfoProvider.deviceId
        val username = createUsername(deviceId)
        return apiResultFlow(
            fetch = { ncmEapiService.registerAnonimous(username) },
            mapSuccess = {
                preferenceStorage.setAnonymousUserId(it.userId)
                preferenceStorage.setLoggedInUserId(null)
            }
        )
    }

    private fun createUsername(deviceId: String): String {
        val key = "".toByteArray()
        val secondString =
            deviceId.toByteArray()
                .mapIndexed { index, oldByte -> oldByte.xor(key[index % key.size]) }
                .toByteArray().md5().encodedAsBase64String()
        return "$deviceId $secondString".toByteArray().encodedAsBase64String()
    }

    val loggedInUserId = preferenceStorage.currentLoggedInUserId

    val guestUserId = preferenceStorage.currentAnonymousUserId

    suspend fun refreshLoginToken() = ncmEapiService.refreshLoginToken()

    val isLoggedIn = loggedInUserId.map { it != null }

    val isLoggedInAsGuest = guestUserId.map { it != null }

    suspend fun logout() {
        ncmEapiService.logout()
        setLoggedInUserId(null)
    }

    fun sendCaptcha(phoneNumber: String) = apiResultFlow(
        fetch = { ncmEapiService.sendSmsCaptcha(phoneNumber) }
    ) { _ ->
    }

    fun verifyPhoneLoginCaptcha(phoneNumber: String, captcha: String) = apiResultFlow(
        fetch = { ncmEapiService.verifyCaptcha(phone = phoneNumber, captcha = captcha) }
    ) { _ -> }


    fun loginWithCaptcha(phoneNumber: String, captcha: String) =
        apiResultFlow(
            fetch = {
                ncmEapiService.cellphoneLogin(
                    phone = phoneNumber,
                    captcha = captcha
                )
            },
            mapSuccess = { result ->
                setLoggedInUserId(result.account.id)
                setAnonymousUserId(null)
                userDao.insertUserProfile(result.profile.asEntity())
                result
            })

    fun loginWithPassword(phoneNumber: String, password: String) =
        apiResultFlow(
            fetch = {
                ncmEapiService.cellphoneLogin(
                    phone = phoneNumber,
                    password = password.toByteArray().md5().toHexString()
                )
            },
            mapSuccess = { result ->
                setLoggedInUserId(result.account.id)
                setAnonymousUserId(null)
                userDao.insertUserProfile(result.profile.asEntity())
                result
            })

    private suspend fun setLoggedInUserId(userId: Long?) {
        preferenceStorage.setLoggedInUserId(userId)
    }

    private suspend fun setAnonymousUserId(userId: Long?) {
        preferenceStorage.setAnonymousUserId(userId)
    }

}