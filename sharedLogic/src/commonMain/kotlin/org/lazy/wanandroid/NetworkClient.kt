package org.lazy.wanandroid

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.lazy.wanandroid.core.data.source.ArticleService
import org.lazy.wanandroid.core.data.source.NetworkDataSource

/** The owner must close this service when its application scope ends. */
fun createArticleService(): ArticleService = NetworkDataSource(createNetworkClient(), platformBaseUrl())

internal val wireJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    encodeDefaults = true
}

internal expect fun platformHttpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient
internal expect fun platformBaseUrl(): String

internal fun createNetworkClient() = platformHttpClient {
    expectSuccess = true
    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
        connectTimeoutMillis = 15_000
        socketTimeoutMillis = 30_000
    }
    install(ContentNegotiation) {
        json(wireJson)
    }
}
