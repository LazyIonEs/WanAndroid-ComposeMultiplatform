package org.lazy.wanandroid.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.lazy.wanandroid.core.network.api.NetworkApi
import org.lazy.wanandroid.core.network.model.ArticleList
import org.lazy.wanandroid.core.network.model.Result

class NetworkDataSource(private val client: HttpClient) {

    companion object {
        const val NETWORK_PAGE_SIZE = 40
    }

    /**
     * 首页文章列表
     * @param page 页码，从0开始
     * @param pageSize 分页数量，取值为[1-40]
     */
    suspend fun articleList(page: Int, pageSize: Int = NETWORK_PAGE_SIZE): Result<ArticleList> {
        val response = client.get(NetworkApi.articleList(page)) {
            contentType(ContentType.Application.Json)
            url {
                parameter("page_size", pageSize)
            }
        }
        val result: Result<ArticleList> = response.body()
        return result
    }
}