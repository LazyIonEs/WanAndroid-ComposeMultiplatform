package org.lazy.wanandroid

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import org.lazy.wanandroid.core.network.api.NetworkApi

internal actual fun platformHttpClient(config: HttpClientConfig<*>.() -> Unit) = HttpClient(OkHttp, config)
internal actual fun platformBaseUrl(): String = NetworkApi.BASE_URL
