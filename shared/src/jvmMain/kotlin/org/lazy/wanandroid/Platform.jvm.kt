package org.lazy.wanandroid

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.apache5.Apache5
import org.jetbrains.compose.resources.FontResource

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun getBaseUrl(): String = "https://www.wanandroid.com/"

actual fun httpClient(config: HttpClientConfig<*>.() -> Unit) = HttpClient(Apache5) {
    config(this)
}

actual fun getPlatformFontResource(): FontResource? = null

@Composable
actual fun getPlatformSpecificFontFamily(): FontFamily? = null