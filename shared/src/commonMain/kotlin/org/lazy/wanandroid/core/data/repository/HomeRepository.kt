package org.lazy.wanandroid.core.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.lazy.wanandroid.core.data.paging.ArticlePagingSource
import org.lazy.wanandroid.core.network.NetworkDataSource
import org.lazy.wanandroid.core.network.model.Article

class HomeRepository(private val network: NetworkDataSource) {

    fun getArticleListStream(): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = NetworkDataSource.NETWORK_PAGE_SIZE,
                initialLoadSize = NetworkDataSource.NETWORK_PAGE_SIZE
            ),
            pagingSourceFactory = { ArticlePagingSource(network) }
        ).flow
    }
}