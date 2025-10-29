package com.github.bumblebee202111.minusonecloudmusic.data.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.ApiResult
import kotlinx.coroutines.Deferred
import java.io.IOException

class ApiLimitOffsetPagingSource<DetailResult : Any, PageResult, PageItem : Any>(
    private val deferredInitialFetch: Deferred<ApiResult<DetailResult>>,
    val getTotalCount: suspend (result: DetailResult) -> Int,
    val getInitialPageItems: suspend (result: DetailResult) -> List<PageItem>,
    val subsequentFetch: suspend (limit: Int, offset: Int, initialResult: DetailResult) -> ApiResult<PageResult>,
    val getSubsequentPageItems: suspend (result: PageResult) -> List<PageItem>,
) :
    PagingSource<Int, PageItem>() {
    override fun getRefreshKey(state: PagingState<Int, PageItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PageItem> {
        val pageIndex = params.key ?: 0
        return if (pageIndex == 0) {
            initialLoad(params)
        } else {
            subsequentLoad(params, pageIndex)
        }
    }

    private lateinit var initialFetchData: DetailResult
    private var totalCount: Int = 0

    private suspend fun initialLoad(params: LoadParams<Int>): LoadResult<Int, PageItem> {
        return try {
            when (val response = deferredInitialFetch.await()) {
                is ApiResult.Success -> {
                    initialFetchData = response.data
                    totalCount = getTotalCount(initialFetchData)
                    LoadResult.Page(
                        data = getInitialPageItems(initialFetchData),
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

    private suspend fun subsequentLoad(
        params: LoadParams<Int>,
        pageIndex: Int
    ): LoadResult<Int, PageItem> {
        val loadSize = params.loadSize
        return try {
            when (val response =
                subsequentFetch(loadSize, pageIndex * loadSize, initialFetchData)) {
                is ApiResult.Success -> {
                    val result = response.data
                    LoadResult.Page(
                        data = getSubsequentPageItems(result),
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
