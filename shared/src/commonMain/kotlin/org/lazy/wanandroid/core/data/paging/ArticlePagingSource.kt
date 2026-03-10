package org.lazy.wanandroid.core.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import org.lazy.wanandroid.core.network.NetworkDataSource
import org.lazy.wanandroid.core.network.model.Article

class ArticlePagingSource(val network: NetworkDataSource) : PagingSource<Int, Article>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        return runCatching {
            val nextPageNumber = params.key ?: 0
            val result = network.articleList(nextPageNumber, params.loadSize)
            if (result.isSuccess()) {
                val articleList = result.data
                val nextKey = if (!articleList.over) {
                    nextPageNumber + 1
                } else {
                    null
                }
                LoadResult.Page(
                    data = articleList.datas,
                    prevKey = null,
                    nextKey = nextKey
                )
            } else {
                LoadResult.Error(Exception(result.errorMsg))
            }
        }.getOrElse {
            LoadResult.Error(it)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}