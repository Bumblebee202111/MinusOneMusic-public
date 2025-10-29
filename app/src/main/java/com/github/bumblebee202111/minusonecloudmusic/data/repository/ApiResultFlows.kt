package com.github.bumblebee202111.minusonecloudmusic.data.repository

import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.map
import com.github.bumblebee202111.minusonecloudmusic.data.AppError
import com.github.bumblebee202111.minusonecloudmusic.data.AppResult
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.ApiResult
import com.github.bumblebee202111.minusonecloudmusic.data.pagingsource.ApiLimitOffsetPagingSource
import com.github.bumblebee202111.minusonecloudmusic.data.pagingsource.apiPageConfig
import com.github.bumblebee202111.minusonecloudmusic.ui.mapper.toAppError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

inline fun <ApiResultDataType:Any, reified ResultType : Any> apiResultFlow(
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
                    "API returned empty success where ${ResultType::class.java.simpleName} was expected."
                )
                emit(AppResult.Error(error))
            }
        }

        is ApiResult.Error -> {
            emit(AppResult.Error(AppError.Server(apiResult.code, apiResult.message)))
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
                        "API returned empty success where ${ResultType::class.java.simpleName} was expected."
                    )
                    emit(AppResult.Error(error))
                }
            }

            is ApiResult.Error -> {
                val serverError = AppError.Server(apiResult.code, apiResult.message)
                emitAll(dbFlow.map { dbData ->
                    AppResult.Error(serverError, data = dbData)
                })
            }
        }
    } catch (e: Exception) {
        emitAll(loadFromDb().map { dbData -> AppResult.Error(e.toAppError(), dbData) })
    }
}

inline fun <DetailResult : Any, PageResult : Any, reified DetailModel : Any, reified PageItem : Any, DomainItem : Any> apiFlowsOfDetailAndPaging(
    scope: CoroutineScope,
    limit: Int,
    noinline initialFetch: suspend (limit: Int) -> ApiResult<DetailResult>,
    noinline mapToDetailModel: suspend (result: DetailResult) -> DetailModel,
    noinline getTotalCount: suspend (DetailResult) -> Int,
    noinline getInitialPageItems: suspend DetailResult.() -> List<PageItem>,
    noinline subsequentFetch: suspend (limit: Int, offset: Int, initialResult: DetailResult) -> ApiResult<PageResult>,
    noinline getSubsequentPageItems: suspend (result: PageResult) -> List<PageItem>,
    crossinline mapToDomainItem: suspend (item: PageItem) -> DomainItem,
): Pair<Flow<AppResult<DetailModel>>, Flow<PagingData<DomainItem>>> {
    val deferredInitialFetch = scope.async {
        initialFetch(limit)
    }

    val appResult: Flow<AppResult<DetailModel>> = apiResultFlow(
        fetch = { deferredInitialFetch.await() },
        mapSuccess = { data ->
            mapToDetailModel(data)
        }
    )
    val pagingData: Flow<PagingData<DomainItem>> = Pager(
        apiPageConfig(limit)
    ) {
        ApiLimitOffsetPagingSource(
            deferredInitialFetch = deferredInitialFetch,
            getTotalCount = getTotalCount,
            getInitialPageItems = getInitialPageItems,
            subsequentFetch = subsequentFetch,
            getSubsequentPageItems = getSubsequentPageItems
        )
    }.flow.map { pagingData -> pagingData.map { mapToDomainItem(it) } }
    return Pair(appResult, pagingData)
}


