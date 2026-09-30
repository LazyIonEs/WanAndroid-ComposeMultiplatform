package org.lazy.wanandroid.core.data.source

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.lazy.wanandroid.core.network.api.NetworkApi
import org.lazy.wanandroid.core.network.model.Article
import org.lazy.wanandroid.core.network.model.ArticleList
import org.lazy.wanandroid.core.network.model.ProjectCategory
import org.lazy.wanandroid.core.network.model.Result

class NetworkDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
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

    override fun close() = client.close()
}

private fun <T> Result<T>.requireData(): T {
    check(isSuccess()) { errorMsg.ifBlank { "请求失败（$errorCode）" } }
    return checkNotNull(data) { "服务器返回空数据" }
}
