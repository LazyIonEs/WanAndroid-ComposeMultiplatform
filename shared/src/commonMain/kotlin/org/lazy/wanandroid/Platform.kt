package org.lazy.wanandroid

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import org.jetbrains.compose.resources.FontResource
import org.koin.core.module.Module

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

const val BASE_URL = "https://www.wanandroid.com/"
const val CORS_URL = "/api/"

expect fun getBaseUrl(): String

expect fun httpClient(config: HttpClientConfig<*>.() -> Unit = {}): HttpClient

expect fun getPlatformFontResource(): FontResource?

const val SETTINGS_NAME = "WanAndroid-Lazy"

expect val platformModule: Module
