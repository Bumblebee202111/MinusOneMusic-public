package com.github.bumblebee202111.minusonecloudmusic.data.repository

import androidx.paging.Pager
import androidx.paging.PagingData
import com.github.bumblebee202111.minusonecloudmusic.data.AppError
import com.github.bumblebee202111.minusonecloudmusic.data.AppResult
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.ApiResult
import com.github.bumblebee202111.minusonecloudmusic.data.pagingsource.ApiLimitOffsetPagingSource
import com.github.bumblebee202111.minusonecloudmusic.data.pagingsource.apiPageConfig
import com.github.bumblebee202111.minusonecloudmusic.ui.mapper.toAppError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    limit: Int,
    noinline initialFetch: suspend (limit: Int) -> ApiResult<DetailResult>,
    noinline mapToDetailModel: suspend (result: DetailResult) -> DetailModel,
    noinline getTotalCount: suspend (DetailResult) -> Int,
    noinline getInitialPageItems: suspend DetailResult.() -> List<PageItem>,
    noinline subsequentFetch: suspend (limit: Int, offset: Int, initialResult: DetailResult) -> ApiResult<PageResult>,
    noinline getSubsequentPageItems: suspend (result: PageResult) -> List<PageItem>,
    noinline onPageLoaded: ((isInitialPage: Boolean, items: List<DomainItem>) -> Unit)? = null,
    crossinline mapToDomainItem: suspend (item: PageItem) -> DomainItem,
): Pair<Flow<AppResult<DetailModel>>, Flow<PagingData<DomainItem>>> {
    val detailState = MutableStateFlow<AppResult<DetailModel>>(AppResult.Loading())

    val pagingData: Flow<PagingData<DomainItem>> = Pager(
        apiPageConfig(limit)
    ) {
        ApiLimitOffsetPagingSource(
            initialFetch = {
                detailState.value = AppResult.Loading(detailState.value.data)
                initialFetch(limit)
            },
            onInitialResult = { apiResult ->
                detailState.value = when (apiResult) {
                    is ApiResult.Success -> AppResult.Success(mapToDetailModel(apiResult.data))
                    is ApiResult.SuccessEmpty -> {
                        if (DetailModel::class == Unit::class) {
                            AppResult.Success(Unit as DetailModel)
                        } else {
                            AppResult.Error(
                                AppError.Unknown(
                                    "EmptyResultError",
                                    "API returned empty success where ${DetailModel::class.java.simpleName} was expected."
                                ),
                                detailState.value.data
                            )
                        }
                    }

                    is ApiResult.Error -> AppResult.Error(
                        AppError.Server(apiResult.code, apiResult.message),
                        detailState.value.data
                    )
                }
            },
            onInitialError = { throwable ->
                detailState.value = AppResult.Error(throwable.toAppError(), detailState.value.data)
            },
            getTotalCount = getTotalCount,
            getInitialPageItems = { result ->
                getInitialPageItems(result).map { mapToDomainItem(it) }
            },
            subsequentFetch = subsequentFetch,
            getSubsequentPageItems = { result ->
                getSubsequentPageItems(result).map { mapToDomainItem(it) }
            },
            onPageLoaded = onPageLoaded
        )
    }.flow
    return Pair(detailState.asStateFlow(), pagingData)
}