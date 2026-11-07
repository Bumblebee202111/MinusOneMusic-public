package com.github.bumblebee202111.minusonecloudmusic.data.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.ApiResult
import kotlinx.coroutines.CancellationException
import java.io.IOException

class ApiLimitOffsetPagingSource<DetailResult : Any, PageResult, PageItem : Any>(
    private val initialFetch: suspend () -> ApiResult<DetailResult>,
    private val onInitialResult: (suspend (ApiResult<DetailResult>) -> Unit)? = null,
    private val onInitialError: ((Throwable) -> Unit)? = null,
    val getTotalCount: suspend (result: DetailResult) -> Int,
    val getInitialPageItems: suspend (result: DetailResult) -> List<PageItem>,
    val subsequentFetch: suspend (limit: Int, offset: Int, initialResult: DetailResult) -> ApiResult<PageResult>,
    val getSubsequentPageItems: suspend (result: PageResult) -> List<PageItem>,
    private val onPageLoaded: ((isInitialPage: Boolean, items: List<PageItem>) -> Unit)? = null,
) :
    PagingSource<Int, PageItem>() {
    override fun getRefreshKey(state: PagingState<Int, PageItem>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PageItem> {
        val pageIndex = params.key ?: 0
        return if (pageIndex == 0) {
            initialLoad(params)
        } else {
            subsequentLoad(params, pageIndex)
        }
    }

    private var initialFetchData: DetailResult? = null
    private var totalCount: Int = 0

    private suspend fun fetchAndStoreInitialData(): LoadResult.Error<Int, PageItem>? {
        val response = initialFetch()
        onInitialResult?.invoke(response)
        return when (response) {
            is ApiResult.Success -> {
                initialFetchData = response.data
                totalCount = getTotalCount(response.data)
                null
            }

            is ApiResult.SuccessEmpty -> {
                LoadResult.Error(IOException("API contract violation: Initial fetch for paging returned an empty success response."))
            }

            is ApiResult.Error -> {
                LoadResult.Error(IOException("API Error on initial fetch: ${response.code} - ${response.message}"))
            }
        }
    }

    private suspend fun initialLoad(params: LoadParams<Int>): LoadResult<Int, PageItem> {
        return try {
            val errorResult = fetchAndStoreInitialData()
            if (errorResult != null) return errorResult
            val data = checkNotNull(initialFetchData)
            val items = getInitialPageItems(data)
            onPageLoaded?.invoke(true, items)
            LoadResult.Page(
                data = items,
                prevKey = null,
                nextKey = if (totalCount > params.loadSize) 1 else null
            )
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            onInitialError?.invoke(ex)
            LoadResult.Error(ex)
        }
    }

    private suspend fun subsequentLoad(
        params: LoadParams<Int>,
        pageIndex: Int
    ): LoadResult<Int, PageItem> {
        val loadSize = params.loadSize
        return try {
            if (initialFetchData == null) {
                val errorResult = fetchAndStoreInitialData()
                if (errorResult != null) return errorResult
            }
            val initialData = checkNotNull(initialFetchData)
            when (val response =
                subsequentFetch(loadSize, pageIndex * loadSize, initialData)) {
                is ApiResult.Success -> {
                    val result = response.data
                    val items = getSubsequentPageItems(result)
                    onPageLoaded?.invoke(false, items)
                    LoadResult.Page(
                        data = items,
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
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            LoadResult.Error(ex)
        }
    }
}