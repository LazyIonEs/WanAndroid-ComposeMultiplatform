package org.lazy.wanandroid.core.data.source

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.parameters
import org.lazy.wanandroid.PersistentCookiesStorage
import org.lazy.wanandroid.core.network.api.NetworkApi
import org.lazy.wanandroid.core.network.model.Article
import org.lazy.wanandroid.core.network.model.ArticleList
import org.lazy.wanandroid.core.network.model.AuthExpiredException
import org.lazy.wanandroid.core.network.model.BannerItem
import org.lazy.wanandroid.core.network.model.CoinInfo
import org.lazy.wanandroid.core.network.model.CoinRankPage
import org.lazy.wanandroid.core.network.model.CoinRecordPage
import org.lazy.wanandroid.core.network.model.ERROR_NOT_LOGGED_IN
import org.lazy.wanandroid.core.network.model.HotKey
import org.lazy.wanandroid.core.network.model.ProjectCategory
import org.lazy.wanandroid.core.network.model.Result
import org.lazy.wanandroid.core.network.model.UserInfo
import org.lazy.wanandroid.core.network.model.UserProfileResponse

class NetworkDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
    private val cookieStorage: PersistentCookiesStorage? = null,
) : ArticleService {
    companion object {
        const val NETWORK_PAGE_SIZE = 20
    }

    override suspend fun articles(section: String, page: Int, categoryId: Int?): ArticleList {
        val path = when (section) {
            "home" -> NetworkApi.articleList(page)
            "plaza" -> NetworkApi.plazaList(page)
            "projects" -> NetworkApi.projectList(page)
            else -> error("Unknown section: $section")
        }
        val response: Result<ArticleList> = client.get(baseUrl + path) {
            parameter("page_size", NETWORK_PAGE_SIZE)
            if (section == "projects") parameter("cid", requireNotNull(categoryId))
        }.body()
        return response.requireData()
    }

    override suspend fun topArticles(): List<Article> =
        client.get(baseUrl + NetworkApi.articleTop()).body<Result<List<Article>>>().requireData()

    override suspend fun categories(): List<ProjectCategory> =
        client.get(baseUrl + NetworkApi.projectCategories()).body<Result<List<ProjectCategory>>>().requireData()

    override suspend fun banners(): List<BannerItem> =
        client.get(baseUrl + NetworkApi.banner()).body<Result<List<BannerItem>>>().requireData()

    override suspend fun hotKeys(): List<HotKey> =
        client.get(baseUrl + NetworkApi.hotKeys()).body<Result<List<HotKey>>>().requireData()

    override suspend fun searchArticles(page: Int, keyword: String): ArticleList =
        client.submitForm(
            url = baseUrl + NetworkApi.search(page),
            formParameters = parameters { append("k", keyword) },
        ) {
            url { parameters.append("page_size", NETWORK_PAGE_SIZE.toString()) }
        }.body<Result<ArticleList>>().requireData()

    override suspend fun login(username: String, password: String): UserInfo =
        client.submitForm(
            url = baseUrl + NetworkApi.login(),
            formParameters = parameters {
                append("username", username)
                append("password", password)
            },
        ).body<Result<UserInfo>>().requireData()

    override suspend fun register(username: String, password: String, repassword: String): UserInfo =
        client.submitForm(
            url = baseUrl + NetworkApi.register(),
            formParameters = parameters {
                append("username", username)
                append("password", password)
                append("repassword", repassword)
            },
        ).body<Result<UserInfo>>().requireData()

    override suspend fun logout() {
        try {
            client.get(baseUrl + NetworkApi.logout()).body<Result<Unit?>>().requireSuccess()
        } finally {
            cookieStorage?.clear()
        }
    }

    override suspend fun userInfo(): UserProfileResponse =
        client.get(baseUrl + NetworkApi.userInfo()).body<Result<UserProfileResponse>>().requireData()

    override suspend fun collectList(page: Int): ArticleList =
        client.get(baseUrl + NetworkApi.collectList(page)) {
            parameter("page_size", NETWORK_PAGE_SIZE)
        }.body<Result<ArticleList>>().requireData()

    override suspend fun collectArticle(articleId: Int) {
        client.post(baseUrl + NetworkApi.collectArticle(articleId))
            .body<Result<Unit?>>()
            .requireSuccess()
    }

    override suspend fun collectExternalArticle(title: String, author: String, link: String): Article =
        client.submitForm(
            url = baseUrl + NetworkApi.collectExternalArticle(),
            formParameters = parameters {
                append("title", title)
                append("author", author)
                append("link", link)
            },
        ).body<Result<Article>>().requireData()

    override suspend fun uncollectOriginId(articleId: Int) {
        client.post(baseUrl + NetworkApi.uncollectOriginId(articleId))
            .body<Result<Unit?>>()
            .requireSuccess()
    }

    override suspend fun uncollectFromMine(id: Int, originId: Int) {
        client.submitForm(
            url = baseUrl + NetworkApi.uncollectFromMine(id),
            formParameters = parameters {
                append("originId", originId.toString())
            },
        ).body<Result<Unit?>>().requireSuccess()
    }

    override suspend fun coinUserInfo(): CoinInfo =
        client.get(baseUrl + NetworkApi.coinUserInfo()).body<Result<CoinInfo>>().requireData()

    override suspend fun coinRank(page: Int): CoinRankPage =
        client.get(baseUrl + NetworkApi.coinRank(page)).body<Result<CoinRankPage>>().requireData()

    override suspend fun coinHistory(page: Int): CoinRecordPage =
        client.get(baseUrl + NetworkApi.coinList(page)) {
            parameter("page_size", NETWORK_PAGE_SIZE)
        }.body<Result<CoinRecordPage>>().requireData()

    override suspend fun hasSavedSession(): Boolean = cookieStorage?.hasAuthCookies() == true

    override suspend fun clearSavedSession() {
        cookieStorage?.clear()
    }

    override fun close() = client.close()
}

private fun Result<*>.requireSuccess() {
    if (errorCode == ERROR_NOT_LOGGED_IN) {
        throw AuthExpiredException(errorMsg.ifBlank { "请先登录" })
    }
    check(isSuccess()) { errorMsg.ifBlank { "请求失败（$errorCode）" } }
}

private fun <T> Result<T>.requireData(): T {
    requireSuccess()
    return checkNotNull(data) { "服务器返回空数据" }
}
