package org.lazy.wanandroid

import kotlinx.serialization.Serializable
import org.lazy.wanandroid.core.network.model.Article

@Serializable
data class FeedState(
    val articles: List<Article> = emptyList(),
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val loadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null,
    val loaded: Boolean = false,
    val failedToLoadMore: Boolean = false,
    // Lets native list sentinels observe progress even when a page adds no new rows.
    val loadedPage: Int? = null,
)
