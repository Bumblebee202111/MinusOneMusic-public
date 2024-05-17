package com.github.bumblebee202111.minusonecloudmusic.data.repository

import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.map
import com.github.bumblebee202111.minusonecloudmusic.data.Result
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.ApiResult
import com.github.bumblebee202111.minusonecloudmusic.data.pagingsource.ApiLimitOffsetPagingSource
import com.github.bumblebee202111.minusonecloudmusic.data.pagingsource.apiPageConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.io.IOException

inline fun <ApiResultDataType, reified ResultType : Any> apiResultFlow(
    crossinline fetch: suspend () -> ApiResult<ApiResultDataType>,
    crossinline mapSuccess: suspend (data: ApiResultDataType) -> ResultType,
) = flow {
    when (val apiResult = fetch()) {
        is ApiResult.ApiSuccessResult -> {
            emit(Result.Success(mapSuccess(apiResult.data)))
        }

        is ApiResult.ApiErrorResult -> {
            emit(Result.Error(Exception("${apiResult.code}: ${apiResult.message}")))
        }
    }
}
    .onStart { emit(Result.Loading()) }
    .catch { emit(Result.Error(Exception(it))) }


inline fun <ApiResultDataType, ResultType> offlineFirstApiResultFlow(
    crossinline loadFromDb: () -> Flow<ResultType>,
    crossinline call: suspend () -> ApiResult<ApiResultDataType>,
    crossinline saveSuccess: suspend (ApiResultDataType) -> Unit,
) = flow {
    val dbFlow = loadFromDb()
    emit(Result.Loading(dbFlow.first()))
    try {
        when (val apiResult = call()) {
            is ApiResult.ApiSuccessResult -> {
                saveSuccess(apiResult.data)
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

inline fun <InitialFetchResultType : Any, NonInitialFetchResultType : Any, reified ResultType : Any, reified PagingValue : Any, ResultItemType : Any> apiDetailFlowWithPagingDataFlow(
    coroutineScope: CoroutineScope,
    limit: Int,
    noinline initialFetch: suspend (limit: Int) -> ApiResult<InitialFetchResultType>,
    noinline mapInitialFetchToResult: suspend InitialFetchResultType.() -> ResultType,
    noinline getTotalCount: suspend InitialFetchResultType.() -> Int,
    noinline nonInitialFetch: suspend (limit: Int,offset: Int, precondition: InitialFetchResultType) -> ApiResult<NonInitialFetchResultType>,
    noinline getPageDataFromInitialFetch: suspend InitialFetchResultType.() -> List<PagingValue>,
    noinline getPageDataFromNonInitialFetch: suspend NonInitialFetchResultType.() -> List<PagingValue>,
    crossinline mapPagingValueToResult: suspend PagingValue.() -> ResultItemType,
): Pair<Flow<Result<ResultType>>, Flow<PagingData<ResultItemType>>> {
    val deferredInitialFetch = coroutineScope.async {
        initialFetch(limit)
    }

    val result: Flow<Result<ResultType>> = apiResultFlow(
        fetch = { deferredInitialFetch.await() },
        mapSuccess = mapInitialFetchToResult
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
    return Pair(result, pagingData)
}
