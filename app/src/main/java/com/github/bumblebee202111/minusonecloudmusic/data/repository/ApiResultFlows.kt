package com.github.bumblebee202111.minusonecloudmusic.data.repository

import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.map
import com.github.bumblebee202111.minusonecloudmusic.data.AppError
import com.github.bumblebee202111.minusonecloudmusic.data.AppResult
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.ApiResult
import com.github.bumblebee202111.minusonecloudmusic.data.pagingsource.ApiLimitOffsetPagingSource
import com.github.bumblebee202111.minusonecloudmusic.data.pagingsource.apiPageConfig
import com.github.bumblebee202111.minusonecloudmusic.utils.toAppError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

inline fun <ApiResultDataType, reified ResultType : Any> apiResultFlow(
    crossinline fetch: suspend () -> ApiResult<ApiResultDataType>,
    crossinline mapSuccess: suspend (data: ApiResultDataType) -> ResultType,
) = flow {
    when (val apiResult = fetch()) {
        is ApiResult.Success -> {
            emit(AppResult.Success(mapSuccess(apiResult.data)))
        }

        is ApiResult.SuccessEmpty -> {
            if (ResultType::class == Unit::class) {
                emit(AppResult.Success(Unit as ResultType))
            } else {
                val error = AppError.Unknown(
                    "EmptyResultError",
                    "API call successful but returned no data where ${ResultType::class.java.simpleName} was expected."
                )
                emit(AppResult.Error(error))
            }
        }

        is ApiResult.Error -> {
            emit(AppResult.Error(AppError.ApiError(apiResult.code, apiResult.message)))
        }
    }
}
    .onStart { emit(AppResult.Loading()) }
    .catch { e ->
        emit(AppResult.Error(e.toAppError()))
    }


inline fun <ApiResultDataType, reified ResultType> offlineFirstApiResultFlow(
    crossinline loadFromDb: () -> Flow<ResultType>,
    crossinline call: suspend () -> ApiResult<ApiResultDataType>,
    crossinline saveSuccess: suspend (ApiResultDataType) -> Unit,
): Flow<AppResult<ResultType>> = flow {
    val dbFlow = loadFromDb()
    emit(AppResult.Loading(dbFlow.first()))
    try {
        when (val apiResult = call()) {
            is ApiResult.Success -> {
                saveSuccess(apiResult.data)
                emitAll(dbFlow.map { AppResult.Success(it) })
            }

            is ApiResult.SuccessEmpty -> {
                if (ResultType::class == Unit::class) {
                    emitAll(dbFlow.map { AppResult.Success(it) })
                } else {
                    val error = AppError.Unknown(
                        "EmptyResultError",
                        "API call successful but returned no data where ${ResultType::class.java.simpleName} was expected."
                    )
                    emit(AppResult.Error(error))
                }
            }

            is ApiResult.Error -> {
                emitAll(dbFlow.map {
                    AppResult.Error(AppError.ApiError(apiResult.code, apiResult.message))
                })
            }
        }
    } catch (e: Exception) {
        emitAll(loadFromDb().map { dbData -> AppResult.Error(e.toAppError(), dbData) })
    }
}

inline fun <InitialFetchResultType : Any, NonInitialFetchResultType : Any, reified ResultType : Any, reified PagingValue : Any, ResultItemType : Any> apiDetailFlowWithPagingDataFlow(
    coroutineScope: CoroutineScope,
    limit: Int,
    noinline initialFetch: suspend (limit: Int) -> ApiResult<InitialFetchResultType>,
    noinline mapInitialFetchToResult: suspend InitialFetchResultType.() -> ResultType,
    noinline getTotalCount: suspend InitialFetchResultType.() -> Int,
    noinline nonInitialFetch: suspend (limit: Int, offset: Int, precondition: InitialFetchResultType) -> ApiResult<NonInitialFetchResultType>,
    noinline getPageDataFromInitialFetch: suspend InitialFetchResultType.() -> List<PagingValue>,
    noinline getPageDataFromNonInitialFetch: suspend NonInitialFetchResultType.() -> List<PagingValue>,
    crossinline mapPagingValueToResult: suspend PagingValue.() -> ResultItemType,
): Pair<Flow<AppResult<ResultType>>, Flow<PagingData<ResultItemType>>> {
    val deferredInitialFetch = coroutineScope.async {
        initialFetch(limit)
    }

    val appResult: Flow<AppResult<ResultType>> = apiResultFlow(
        fetch = { deferredInitialFetch.await() },
        mapSuccess = { data ->
            data.mapInitialFetchToResult()
        }
    )
    val pagingData: Flow<PagingData<ResultItemType>> = Pager(
        apiPageConfig(limit)
    ) {
        ApiLimitOffsetPagingSource(
            deferredInitialFetch = deferredInitialFetch,
            nonInitialFetch = nonInitialFetch,
            getTotalCount = getTotalCount,
            mapToFirstPageData = getPageDataFromInitialFetch,
            mapToNonFirstPageData = getPageDataFromNonInitialFetch
        )
    }.flow.map { pagingData -> pagingData.map { mapPagingValueToResult(it) } }
    return Pair(appResult, pagingData)
}


