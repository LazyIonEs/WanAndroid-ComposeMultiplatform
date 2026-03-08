package org.lazy.wanandroid

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import org.jetbrains.compose.resources.FontResource

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun getBaseUrl(): String

expect fun httpClient(config: HttpClientConfig<*>.() -> Unit = {}): HttpClient

expect fun getPlatformFontResource(): FontResource?

@Composable
expect fun getPlatformSpecificFontFamily(): FontFamily?