package com.github.bumblebee202111.minusonecloudmusic.data.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.ApiResult
import kotlinx.coroutines.Deferred
import java.io.IOException

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
                is ApiResult.Success -> {
                    initialFetchData = response.data
                    totalCount = getTotalCount(initialFetchData)
                    LoadResult.Page(
                        data = mapToFirstPageData(initialFetchData),
                        prevKey = null,
                        nextKey = if (totalCount > params.loadSize) 1 else null
                    )
                }

                is ApiResult.SuccessEmpty -> {
                    LoadResult.Error(IOException("API contract violation: Initial fetch for paging returned an empty success response."))
                }

                is ApiResult.Error -> {
                    LoadResult.Error(IOException("API Error on initial fetch: ${response.code} - ${response.message}"))
                }
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
                is ApiResult.Success -> {
                    val result = response.data
                    LoadResult.Page(
                        data = mapToNonFirstPageData(result),
                        prevKey = null,
                        nextKey = if (totalCount > (pageIndex + 1) * loadSize) pageIndex + 1 else null
                    )
                }

                is ApiResult.SuccessEmpty -> {
                    LoadResult.Error(IOException("API contract violation: Page fetch for index $pageIndex returned an empty success response."))
                }

                is ApiResult.Error -> {
                    LoadResult.Error(IOException("API Error on page $pageIndex: ${response.code} - ${response.message}"))
                }
            }
        } catch (ex: Exception) {
            LoadResult.Error(ex)
        }
    }
}
