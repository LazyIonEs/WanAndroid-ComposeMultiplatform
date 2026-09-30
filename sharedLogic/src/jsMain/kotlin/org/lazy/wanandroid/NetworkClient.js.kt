package org.lazy.wanandroid

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.js.Js

internal actual fun platformHttpClient(config: HttpClientConfig<*>.() -> Unit) = HttpClient(Js, config)
internal actual fun platformBaseUrl(): String = "/api/"
