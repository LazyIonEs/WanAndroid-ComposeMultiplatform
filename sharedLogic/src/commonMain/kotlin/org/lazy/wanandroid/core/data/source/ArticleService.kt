package org.lazy.wanandroid.core.data.source

import org.lazy.wanandroid.core.network.model.Article
import org.lazy.wanandroid.core.network.model.ArticleList
import org.lazy.wanandroid.core.network.model.BannerItem
import org.lazy.wanandroid.core.network.model.CoinInfo
import org.lazy.wanandroid.core.network.model.CoinRankPage
import org.lazy.wanandroid.core.network.model.CoinRecordPage
import org.lazy.wanandroid.core.network.model.HotKey
import org.lazy.wanandroid.core.network.model.ProjectCategory
import org.lazy.wanandroid.core.network.model.UserInfo
import org.lazy.wanandroid.core.network.model.UserProfileResponse

interface ArticleService {
    suspend fun articles(section: String, page: Int, categoryId: Int?): ArticleList
    suspend fun topArticles(): List<Article>
    suspend fun categories(): List<ProjectCategory>

    suspend fun banners(): List<BannerItem> = emptyList()
    suspend fun hotKeys(): List<HotKey> = emptyList()
    suspend fun searchArticles(page: Int, keyword: String): ArticleList = throw UnsupportedOperationException()

    suspend fun login(username: String, password: String): UserInfo = throw UnsupportedOperationException()
    suspend fun register(username: String, password: String, repassword: String): UserInfo = throw UnsupportedOperationException()
    suspend fun logout() = Unit
    suspend fun userInfo(): UserProfileResponse = throw UnsupportedOperationException()

    suspend fun collectList(page: Int): ArticleList = throw UnsupportedOperationException()
    suspend fun collectArticle(articleId: Int) = Unit
    suspend fun collectExternalArticle(title: String, author: String, link: String): Article = throw UnsupportedOperationException()
    suspend fun uncollectOriginId(articleId: Int) = Unit
    suspend fun uncollectFromMine(id: Int, originId: Int) = Unit

    suspend fun coinUserInfo(): CoinInfo = throw UnsupportedOperationException()
    suspend fun coinRank(page: Int): CoinRankPage = throw UnsupportedOperationException()
    suspend fun coinHistory(page: Int): CoinRecordPage = throw UnsupportedOperationException()

    suspend fun hasSavedSession(): Boolean = false
    suspend fun clearSavedSession() = Unit

    fun close() {}
}
