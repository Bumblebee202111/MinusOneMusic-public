package com.github.bumblebee202111.minusonecloudmusic.data.repository

import com.github.bumblebee202111.minusonecloudmusic.data.Result
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.io.IOException



inline fun <ApiResultDataType, reified ResultType : Any> apiResultFlow(
    crossinline fetcher: suspend () -> ApiResult<ApiResultDataType>,
    crossinline successMapper: suspend (data: ApiResultDataType) -> ResultType,
) = flow {
    when(val apiResult=fetcher()) {
        is ApiResult.ApiSuccessResult -> {
            emit(Result.Success(successMapper(apiResult.data)))
        }
        is ApiResult.ApiErrorResult -> {
            emit(Result.Error(Exception("${apiResult.code}: ${apiResult.message}")))
        }
    }
}
    .onStart { emit(Result.Loading())}
    .catch { emit(Result.Error(Exception(it))) }


inline fun <ApiResultDataType, ResultType> offlineFirstApiResultFlow(
    crossinline loadFromDb: () -> Flow<ResultType>,
    crossinline call: suspend () -> ApiResult<ApiResultDataType>,
    crossinline successSaver: suspend (ApiResultDataType) -> Unit,
) =  flow {
    val dbFlow = loadFromDb()
    emit(Result.Loading(dbFlow.first()))
    try {
        when (val apiResult = call()) {
            is ApiResult.ApiSuccessResult -> {
                successSaver(apiResult.data)
                emitAll(dbFlow.map { Result.Success(it) })
            }

            is ApiResult.ApiErrorResult -> {
                emitAll(dbFlow.map {
                    Result.Error(
                        Exception("${apiResult.code}: ${apiResult.message}"),
                        it
                    )
                })
            }
        }
    } catch (exception: IOException) {
        emitAll(loadFromDb().map { Result.Error(exception, it) })
    }
}