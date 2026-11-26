package org.lazy.wanandroid

import kotlinx.serialization.Serializable
import org.lazy.wanandroid.core.network.model.BannerItem
import org.lazy.wanandroid.core.network.model.CoinInfo
import org.lazy.wanandroid.core.network.model.CoinRecord
import org.lazy.wanandroid.core.network.model.HotKey
import org.lazy.wanandroid.core.network.model.ProjectCategory
import org.lazy.wanandroid.core.network.model.UserInfo

@Serializable
data class CoinRankState(
    val items: List<CoinInfo> = emptyList(),
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val loadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null,
    val loaded: Boolean = false,
    val failedToLoadMore: Boolean = false,
    val loadedPage: Int? = null,
)

@Serializable
data class CoinHistoryState(
    val items: List<CoinRecord> = emptyList(),
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val loadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null,
    val loaded: Boolean = false,
    val failedToLoadMore: Boolean = false,
    val loadedPage: Int? = null,
)

@Serializable
data class WanAndroidState(
    val home: FeedState = FeedState(),
    val plaza: FeedState = FeedState(),
    val projects: FeedState = FeedState(),
    val categories: List<ProjectCategory> = emptyList(),
    val selectedProjectId: Int? = null,
    val categoriesLoading: Boolean = false,
    val categoriesError: String? = null,
    val banners: List<BannerItem> = emptyList(),
    val hotKeys: List<HotKey> = emptyList(),
    val searchHistory: List<String> = emptyList(),
    val searchQuery: String = "",
    val search: FeedState = FeedState(hasMore = false),
    val user: UserInfo? = null,
    val coinInfo: CoinInfo? = null,
    val authLoading: Boolean = false,
    val authError: String? = null,
    val collectedIds: List<Int> = emptyList(),
    val collections: FeedState = FeedState(),
    val coinRank: CoinRankState = CoinRankState(),
    val coinHistory: CoinHistoryState = CoinHistoryState(),
    val actionMessage: String? = null,
) {
    fun feed(section: String): FeedState = when (section) {
        "home" -> home
        "plaza" -> plaza
        "projects" -> projects
        "search" -> search
        "collect" -> collections
        else -> error("Unknown section: $section")
    }
}
