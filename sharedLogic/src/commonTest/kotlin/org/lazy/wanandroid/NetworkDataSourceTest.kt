package org.lazy.wanandroid

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import org.lazy.wanandroid.core.data.source.NetworkDataSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class NetworkDataSourceTest {
    @Test
    fun requestsUseOfficialPathsFixedPageSizeAndProjectCategory() = runTest {
        val requestedPaths = mutableListOf<String>()
        val client = HttpClient(MockEngine { request ->
            requestedPaths += request.url.encodedPath
            assertEquals("20", request.url.parameters["page_size"])
            assertEquals(listOf("20"), request.url.parameters.getAll("page_size"))
            if (request.url.encodedPath.startsWith("/project/")) {
                assertEquals("294", request.url.parameters["cid"])
            } else {
                assertNull(request.url.parameters["cid"])
            }
            respond(PAGE_JSON, headers = JSON_HEADERS)
        }) {
            install(ContentNegotiation) { json(wireJson) }
        }
        try {
            val source = NetworkDataSource(client, "https://www.wanandroid.com/")
            source.articles("home", 0, null)
            source.articles("plaza", 0, null)
            source.articles("projects", 1, 294)
            source.articles("projects", 2, 294)

            assertEquals(listOf(
                "/article/list/0/json",
                "/user_article/list/0/json",
                "/project/list/1/json",
                "/project/list/2/json",
            ), requestedPaths)
        } finally {
            client.close()
        }
    }

    @Test
    fun articleResponseAllowsAbsentOptionalFieldsAndUnknownServerFields() = runTest {
        val client = mockClient(PAGE_JSON)
        try {
            val result = NetworkDataSource(client, "https://www.wanandroid.com/")
                .articles("plaza", 0, null)
            val article = result.datas.single()
            assertEquals("Compose &amp; Kotlin", article.title)
            assertEquals("https://example.com/article", article.link)
            assertNull(article.id)
            assertNull(article.author)
            assertNull(article.desc)
            assertNull(article.tags)
            assertEquals(1, result.curPage)
        } finally {
            client.close()
        }
    }

    @Test
    fun apiErrorWithNullDataReportsServerMessage() = runTest {
        val client = mockClient("""{"data":null,"errorCode":-1001,"errorMsg":"请先登录"}""")
        try {
            val source = NetworkDataSource(client, "https://www.wanandroid.com/")
            val error = assertFailsWith<IllegalStateException> {
                source.articles("plaza", 0, null)
            }
            assertEquals("请先登录", error.message)
        } finally {
            client.close()
        }
    }

    @Test
    fun successfulEnvelopeWithNullDataIsAnErrorInsteadOfAnEmptySuccess() = runTest {
        val client = mockClient("""{"data":null,"errorCode":0,"errorMsg":""}""")
        try {
            val source = NetworkDataSource(client, "https://www.wanandroid.com/")
            val error = assertFailsWith<IllegalStateException> { source.categories() }
            assertEquals("服务器返回空数据", error.message)
        } finally {
            client.close()
        }
    }

    @Test
    fun categoryAndPinnedEndpointsDecodeTheirEnvelopes() = runTest {
        val requestedPaths = mutableListOf<String>()
        val client = HttpClient(MockEngine { request ->
            requestedPaths += request.url.encodedPath
            val body = when (request.url.encodedPath) {
                "/project/tree/json" -> """{"data":[{"id":294,"name":"完整项目","children":[]}],"errorCode":0,"errorMsg":""}"""
                "/article/top/json" -> """{"data":[{"id":1,"title":"置顶","link":"https://example.com/top"}],"errorCode":0,"errorMsg":""}"""
                else -> error("Unexpected path: ${request.url.encodedPath}")
            }
            respond(body, headers = JSON_HEADERS)
        }) {
            install(ContentNegotiation) { json(wireJson) }
        }
        try {
            val source = NetworkDataSource(client, "https://www.wanandroid.com/")
            assertEquals(294, source.categories().single().id)
            assertEquals("置顶", source.topArticles().single().title)
            assertEquals(listOf("/project/tree/json", "/article/top/json"), requestedPaths)
        } finally {
            client.close()
        }
    }
}

private fun mockClient(body: String) = HttpClient(MockEngine {
    respond(body, headers = JSON_HEADERS)
}) {
    install(ContentNegotiation) { json(wireJson) }
}

private val JSON_HEADERS = headersOf(HttpHeaders.ContentType, "application/json")

private const val PAGE_JSON = """{
  "errorCode": 0,
  "errorMsg": "",
  "data": {
    "curPage": 1,
    "datas": [{"title":"Compose &amp; Kotlin","link":"https://example.com/article","unknownServerField":true}],
    "offset": 0,
    "over": false,
    "pageCount": 3,
    "size": 20,
    "total": 60
  }
}"""
