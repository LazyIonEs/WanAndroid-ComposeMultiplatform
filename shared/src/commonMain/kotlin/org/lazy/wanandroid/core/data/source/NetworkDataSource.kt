package org.lazy.wanandroid.core.data.source

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.lazy.wanandroid.core.network.api.NetworkApi
import org.lazy.wanandroid.core.network.model.Article
import org.lazy.wanandroid.core.network.model.ArticleList
import org.lazy.wanandroid.core.network.model.Result

class NetworkDataSource(private val client: HttpClient) {

    companion object {
        const val NETWORK_PAGE_SIZE = 40
    }

    /**
     * 获取首页文章列表
     *
     * @param page 页码，从0开始
     * @param pageSize 分页数量，取值范围为 [1, 40]
     * @return 包含文章列表数据的 [org.lazy.wanandroid.core.network.model.Result] 对象
     */
    suspend fun articleList(page: Int, pageSize: Int): Result<ArticleList> {
        val response = client.get(NetworkApi.articleList(page)) {
            contentType(ContentType.Application.Json)
            url {
                parameter("page_size", pageSize)
            }
        }
        val result: Result<ArticleList> = response.body()
        return result
    }

    /**
     * 置顶文章列表
     *
     * @return 包含置顶文章列表的 [Result] 对象
     */
    suspend fun articleTop(): Result<List<Article>> {
        val response = client.get(NetworkApi.articleTop()) {
            contentType(ContentType.Application.Json)
        }
        val result: Result<List<Article>> = response.body()
        return result
    }
}