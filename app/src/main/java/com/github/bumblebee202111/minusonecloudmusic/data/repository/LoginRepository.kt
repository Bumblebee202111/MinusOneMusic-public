package com.github.bumblebee202111.minusonecloudmusic.data.repository

import androidx.room.withTransaction
import com.github.bumblebee202111.minusonecloudmusic.data.Result
import com.github.bumblebee202111.minusonecloudmusic.data.database.AppDatabase
import com.github.bumblebee202111.minusonecloudmusic.data.datasource.NetworkDataSource
import com.github.bumblebee202111.minusonecloudmusic.data.datastore.PreferenceStorage
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.ApiResult
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.login.LoginQrCodeStatus
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.login.LoginQrCodeStatusCodes
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.login.asLoginQrCodeStatus
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.user.asEntity
import com.github.bumblebee202111.minusonecloudmusic.data.network.util.encodedAsBase64String
import com.github.bumblebee202111.minusonecloudmusic.data.network.util.md5
import com.github.bumblebee202111.minusonecloudmusic.utils.DeviceInfoProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.experimental.xor

@OptIn(ExperimentalStdlibApi::class)
@Singleton
class LoginRepository @Inject constructor(
    private val preferenceStorage: PreferenceStorage,
    private val appDatabase: AppDatabase,
    private val networkDataSource: NetworkDataSource,
    private val deviceInfoProvider: DeviceInfoProvider
) {
    private val userDao = appDatabase.userDao()
    private var loginQrCodeKey: String? = null

    fun registerAnonymous(): Flow<Result<Unit?>> {
        val deviceId =
            deviceInfoProvider.deviceId
        val username = createUsername(deviceId)
        return apiResultFlow(
            fetch = { networkDataSource.registerAnonimous(username) },
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

    fun getLoginQrCode(): Flow<Result<String>> {
        return flow {
            try {
                when (val loginQrCodeKeyResponse = networkDataSource.getLoginQrCodeKey()) {
                    is ApiResult.ApiSuccessResult -> {
                        loginQrCodeKey = loginQrCodeKeyResponse.data.unikey
                        val loginQrCodeResponse =
                            networkDataSource.getLoginQrCode(loginQrCodeKey!!)
                        when (loginQrCodeResponse) {
                            is ApiResult.ApiSuccessResult -> {
                                emit(Result.Success(loginQrCodeResponse.data.qrimg))
                            }
                            is ApiResult.ApiErrorResult -> {
                                emit(Result.Error(Exception(loginQrCodeResponse.message)))
                            }
                        }
                    }
                    is ApiResult.ApiErrorResult -> {
                        emit(Result.Error(Exception(loginQrCodeKeyResponse.message)))
                    }
                }
            } catch (e: Exception) {
                emit(Result.Error(e))
            }
        }
    }

    fun getLoginQrCodeStatus(): Flow<Result<LoginQrCodeStatus>> {
        val refreshIntervalMs: Long = 3000
        return flow {
            while (true) {
                try {
                    loginQrCodeKey?.let {
                        val loginQrCodeStatusResponse = networkDataSource.checkLoginQrCode(it)
                        if (loginQrCodeStatusResponse.code >= 800) {
                            if (loginQrCodeStatusResponse.code == LoginQrCodeStatusCodes.SUCCESS) {
                                networkDataSource.getLoginStatus().let { loginStatusResponse ->
                                    if (loginStatusResponse is ApiResult.ApiSuccessResult)
                                        setLoggedInUserId(loginStatusResponse.data.account.id)
                                }

                            }
                            emit(Result.Success(loginQrCodeStatusResponse.asLoginQrCodeStatus()))
                            if (loginQrCodeStatusResponse.code == LoginQrCodeStatusCodes.EXPIRED || loginQrCodeStatusResponse.code == LoginQrCodeStatusCodes.SUCCESS) {
                                loginQrCodeKey = null
                                return@flow
                            }

                        } else {
                            emit(Result.Error(Exception("${loginQrCodeStatusResponse.code}: ${loginQrCodeStatusResponse.message}")))
                        }
                    }

                } catch (e: Exception) {
                    emit(Result.Error(e))
                }
                delay(refreshIntervalMs)
            }
        }
    }

    val loggedInUserId = preferenceStorage.currentLoggedInUserId

    val guestUserId = preferenceStorage.currentAnonymousUserId

    @ScheduledForRemoval
    @Deprecated("Won't work alone")
    fun getLoginStatus() = offlineFirstApiResultFlow(
        loadFromDb = {
            loggedInUserId.flatMapLatest {
                if (it != null)
                    appDatabase.userDao().observeUserProfile(it)
                else flowOf(null)
            }
        },
        call = { networkDataSource.getLoginStatus() },
        saveSuccess = {
            if (it.account.status == 0) {
                appDatabase.userDao().insertUserProfile(it.profile!!.asEntity())
                setLoggedInUserId(it.account.id)
            } else {
                setLoggedInUserId(null)
            }
        }
    )

    suspend fun refreshLoginToken() = networkDataSource.refreshLoginToken()

    val isLoggedIn = loggedInUserId.map { it != null }

    val isLoggedInAsGuest = guestUserId.map { it != null }

    suspend fun logout() {
        networkDataSource.logout()
        setLoggedInUserId(null)
    }

    fun sendCaptcha(phoneNumber: String) = apiResultFlow(
        fetch = { networkDataSource.sendSMSCaptcha(phoneNumber) }
    ) { _ ->
    }

    fun verifyPhoneLoginCaptcha(phoneNumber: String, captcha: String) = apiResultFlow(
        fetch = { networkDataSource.verifyCaptcha(phoneNumber, captcha) }
    ) { _ -> }


    fun loginWithCaptcha(phoneNumber: String, captcha: String) =
        apiResultFlow(fetch = {
            networkDataSource.cellphoneLogin(
                cellphone = phoneNumber,
                captcha = captcha
            )
        },
            mapSuccess = { result ->
                setLoggedInUserId(result.account.id)
                setAnonymousUserId(null)
                result
            })

    fun loginWithPassword(phoneNumber: String, password: String) =
        apiResultFlow(fetch = {
            networkDataSource.cellphoneLogin(
                cellphone = phoneNumber,
                password = password.toByteArray().md5().toHexString()
            )
        },
            mapSuccess = { result ->
                setLoggedInUserId(result.account.id)
                setAnonymousUserId(null)
                appDatabase.withTransaction {
                    userDao.insertUserProfile(result.profile.asEntity())
                }
                result
            })

    private suspend fun setLoggedInUserId(userId: Long?) {
        preferenceStorage.setLoggedInUserId(userId)
    }

    private suspend fun setAnonymousUserId(userId: Long?) {
        preferenceStorage.setAnonymousUserId(userId)
    }

}