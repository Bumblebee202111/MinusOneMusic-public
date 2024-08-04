package com.github.bumblebee202111.minusonecloudmusic.data.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.ApiResult
import kotlinx.coroutines.Deferred

class ApiLimitOffsetPagingSource<InitialFetchResultType : Any, NonInitialFetchResultType, Value : Any>(
    private val deferredInitialFetch: Deferred<ApiResult<InitialFetchResultType>>,
    val nonInitialFetch: suspend (limit: Int, offset: Int, precondition: InitialFetchResultType) -> ApiResult<NonInitialFetchResultType>,
    val getTotalCount: suspend InitialFetchResultType.() -> Int,
    val mapToFirstPageData: suspend InitialFetchResultType.() -> List<Value>,
    val mapToNonFirstPageData: suspend NonInitialFetchResultType.() -> List<Value>,
) :
    PagingSource<Int, Value>() {
    override fun getRefreshKey(state: PagingState<Int, Value>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Value> {
        val pageIndex = params.key ?: 0
        return if (pageIndex == 0) {
            initialLoad(params)
        } else {
            nonInitialLoad(params, pageIndex)
        }
    }

    private lateinit var initialFetchData: InitialFetchResultType
    private var totalCount: Int = 0
    private suspend fun initialLoad(params: LoadParams<Int>): LoadResult<Int, Value> {
        return try {
            when (val response = deferredInitialFetch.await()) {
                is ApiResult.ApiSuccessResult -> {
                    initialFetchData = response.data
                    totalCount = getTotalCount(initialFetchData)
                    LoadResult.Page(
                        data = mapToFirstPageData(initialFetchData),
                        prevKey = null,
                        nextKey = if (totalCount > params.loadSize) 1 else null
                    )
                }

                else -> LoadResult.Invalid()
            }
        } catch (ex: Exception) {
            LoadResult.Error(ex)
        }
    }

    private suspend fun nonInitialLoad(
        params: LoadParams<Int>,
        pageIndex: Int
    ): LoadResult<Int, Value> {
        val loadSize = params.loadSize
        return try {
            when (val response =
                nonInitialFetch(loadSize, pageIndex * loadSize, initialFetchData)) {
                is ApiResult.ApiSuccessResult -> {
                    val result = response.data
                    LoadResult.Page(
                        data = mapToNonFirstPageData(result),
                        prevKey = null,
                        nextKey = if (totalCount > (pageIndex + 1) * loadSize) pageIndex + 1 else null
                    )
                }

                else -> LoadResult.Invalid()
            }
        } catch (ex: Exception) {
            LoadResult.Error(ex)
        }
    }
}
